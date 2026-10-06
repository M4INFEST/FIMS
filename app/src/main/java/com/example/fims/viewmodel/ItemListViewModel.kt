package com.example.fims.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fims.data.InventoryRepository
import com.example.fims.data.ItemWithStock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

sealed interface ItemListUiState {
    data object Loading : ItemListUiState
    data class Success(val items: List<ItemWithStock>) : ItemListUiState
    data class Error(val message: String) : ItemListUiState
}

class ItemListViewModel(private val repository: InventoryRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<ItemListUiState> = combine(
        repository.getItemsWithStock(),
        _searchQuery
    ) { items, query ->
        val filtered = if (query.isBlank()) {
            items
        } else {
            items.filter {
                it.item.name.contains(query, ignoreCase = true) ||
                it.item.category.contains(query, ignoreCase = true)
            }
        }
        ItemListUiState.Success(filtered) as ItemListUiState
    }
    .onStart { delay(600) } // Simulate network/DB delay for shimmer effect
    .catch { e -> emit(ItemListUiState.Error(e.message ?: "Unknown error")) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ItemListUiState.Loading
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class ItemListViewModelFactory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}