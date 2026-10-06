package com.example.fims.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fims.data.FoodItem
import com.example.fims.data.InventoryRepository
import kotlinx.coroutines.launch

class AddEditItemViewModel(private val repository: InventoryRepository) : ViewModel() {
    var name by mutableStateOf("")
    var category by mutableStateOf("")
    var unit by mutableStateOf("")
    var reorderLevel by mutableStateOf("")
    var errorMessage by mutableStateOf("")

    fun saveItem(onSuccess: () -> Unit) {
        val reorder = reorderLevel.toIntOrNull()
        if (name.isBlank() || category.isBlank() || unit.isBlank()) {
            errorMessage = "Please fill out all text fields."
            return
        }
        if (reorder == null || reorder < 0) {
            errorMessage = "Reorder level must be a valid positive number."
            return
        }

        viewModelScope.launch {
            repository.insertFoodItem(
                FoodItem(
                    name = name,
                    category = category,
                    unit = unit,
                    reorderLevel = reorder
                )
            )
            onSuccess()
        }
    }
}

class AddEditItemViewModelFactory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}