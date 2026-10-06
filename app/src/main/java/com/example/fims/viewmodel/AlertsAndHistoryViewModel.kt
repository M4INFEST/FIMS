package com.example.fims.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fims.data.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AlertsAndHistoryViewModel(repository: InventoryRepository) : ViewModel() {
    val history = repository.getTransactionHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    private val now = System.currentTimeMillis()
    private val threeDays = 3L * 24 * 60 * 60 * 1000
    
    val expiredBatches = repository.getExpiredBatches(now)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val nearExpiryBatches = repository.getNearExpiryBatches(now, now + threeDays)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class AlertsAndHistoryViewModelFactory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsAndHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsAndHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}