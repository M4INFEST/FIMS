package com.example.fims.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fims.data.InventoryRepository
import com.example.fims.data.TransactionHistoryItem
import com.example.fims.data.TransactionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onStart
import java.util.Calendar

/**
 * Enterprise-grade UI State for the Dashboard.
 * Utilizes sealed interfaces to guarantee exhaustive state handling in the UI layer.
 */
sealed interface DashboardUiState {
    /** Represents the initial fetching state (used to trigger Shimmer animations) */
    data object Loading : DashboardUiState
    
    /** Represents a failure in combining or fetching the database flows */
    data class Error(val message: String) : DashboardUiState
    
    /** 
     * Represents a fully successfully loaded dashboard state.
     * Contains exhaustive analytics for the current day.
     */
    data class Success(
        val totalItems: Int,
        val normalStockCount: Int,
        val lowStockCount: Int,
        val outOfStockCount: Int,
        val nearExpiryCount: Int,
        val expiredCount: Int,
        val todayStockInValue: Double,
        val todayStockOutValue: Double,
        val recentTransactions: List<TransactionHistoryItem>
    ) : DashboardUiState
}

/**
 * Advanced ViewModel responsible for aggregating data across multiple database tables.
 * Uses Kotlin Coroutine Flows to reactively push updates to the UI thread efficiently.
 */
class DashboardViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val now = System.currentTimeMillis()
    private val threeDaysMillis = 3L * 24 * 60 * 60 * 1000

    /**
     * Combined Flow aggregating Items, Batches, and Transactions.
     * This is an advanced reactive pattern ensuring the dashboard is ALWAYS perfectly in sync
     * without requiring manual refresh triggers.
     */
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getItemsWithStock(),
        repository.getExpiredBatches(now),
        repository.getNearExpiryBatches(now, now + threeDaysMillis),
        repository.getTransactionHistory()
    ) { itemsWithStock, expiredBatches, nearExpiryBatches, transactions ->
        
        var total = itemsWithStock.size
        var normal = 0
        var low = 0
        var outOfStock = 0

        itemsWithStock.forEach { itemWithStock ->
            when {
                itemWithStock.totalStock == 0 -> outOfStock++
                itemWithStock.totalStock <= itemWithStock.item.reorderLevel -> low++
                else -> normal++
            }
        }

        // Calculate today's boundaries for analytics
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        var stockInToday = 0.0
        var stockOutToday = 0.0

        val recentTxs = transactions.take(5) // Get latest 5 for the mini-feed

        transactions.filter { it.transaction.createdAt >= startOfToday }.forEach { txItem ->
            // In a real enterprise app, we'd multiply by batch cost. 
            // Here we use quantity as the metric for the chart payload.
            when (txItem.transaction.type) {
                TransactionType.STOCK_IN -> stockInToday += txItem.transaction.quantity
                TransactionType.STOCK_OUT, TransactionType.WASTAGE -> stockOutToday += txItem.transaction.quantity
                else -> {}
            }
        }

        DashboardUiState.Success(
            totalItems = total,
            normalStockCount = normal,
            lowStockCount = low,
            outOfStockCount = outOfStock,
            nearExpiryCount = nearExpiryBatches.size,
            expiredCount = expiredBatches.size,
            todayStockInValue = stockInToday,
            todayStockOutValue = stockOutToday,
            recentTransactions = recentTxs
        ) as DashboardUiState
    }
    .onStart { 
        // Simulate a slight network/DB delay to show off beautiful Shimmer Loading UI
        delay(800) 
    }
    .catch { e ->
        emit(DashboardUiState.Error(e.message ?: "Unknown database error occurred"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )
}

class DashboardViewModelFactory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}