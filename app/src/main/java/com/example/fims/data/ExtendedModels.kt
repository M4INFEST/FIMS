package com.example.fims.data

import androidx.room.Embedded

data class BatchInfo(
    @Embedded val batch: Batch,
    val itemName: String,
    val itemUnit: String
)

data class TransactionHistoryItem(
    @Embedded val transaction: StockTransaction,
    val itemName: String
)