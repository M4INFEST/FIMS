package com.example.fims.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fims.data.Batch
import com.example.fims.data.InventoryRepository
import com.example.fims.data.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TransactionsViewModel(private val repository: InventoryRepository) : ViewModel() {
    val activeItems = repository.getActiveFoodItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    suspend fun saveStockIn(itemId: Int, quantity: Int, batchNo: String, cost: Double, expiry: Long, reason: String): Result<Unit> {
        return try {
            if (quantity <= 0) throw Exception("Quantity must be greater than 0")
            if (cost < 0) throw Exception("Cost cannot be negative")
            repository.stockIn(
                Batch(itemId = itemId, batchNo = batchNo, expiryDate = expiry, quantity = quantity, unitCost = cost, receivedDate = System.currentTimeMillis()),
                reason
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveStockOutOrWastage(itemId: Int, quantity: Int, isWastage: Boolean, reason: String): Result<Unit> {
        return try {
            if (quantity <= 0) throw Exception("Quantity must be greater than 0")
            val type = if (isWastage) TransactionType.WASTAGE else TransactionType.STOCK_OUT
            repository.stockOutOrWastage(itemId, quantity, type, reason)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class TransactionsViewModelFactory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}