package com.example.fims.data

import androidx.room.Embedded

data class ItemWithStock(
    @Embedded val item: FoodItem,
    val totalStock: Int
)