package com.example.fims.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val itemId: Int = 0,
    val name: String,
    val category: String,
    val unit: String,
    val reorderLevel: Int,
    val isActive: Boolean = true
)