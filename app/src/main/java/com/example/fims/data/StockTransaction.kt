package com.example.fims.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    STOCK_IN, STOCK_OUT, WASTAGE, ADJUSTMENT
}

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Batch::class,
            parentColumns = ["batchId"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("itemId"), Index("batchId")]
)
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val txnId: Int = 0,
    val itemId: Int,
    val batchId: Int?,
    val type: TransactionType,
    val quantity: Int,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)