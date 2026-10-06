package com.example.fims.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "batches",
    foreignKeys = [
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("itemId")]
)
data class Batch(
    @PrimaryKey(autoGenerate = true)
    val batchId: Int = 0,
    val itemId: Int,
    val batchNo: String,
    val expiryDate: Long, // Epoch millis
    val quantity: Int,
    val unitCost: Double,
    val receivedDate: Long
)