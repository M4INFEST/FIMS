package com.example.fims.data

import kotlinx.coroutines.flow.Flow

import androidx.room.withTransaction

class InventoryRepository(private val dao: FimsDao, private val db: FimsDatabase) {
    fun getActiveFoodItems(): Flow<List<FoodItem>> = dao.getActiveFoodItems()
    
    fun getItemsWithStock(): Flow<List<ItemWithStock>> = dao.getItemsWithStock()

    suspend fun getFoodItemById(id: Int): FoodItem? = dao.getFoodItemById(id)

    suspend fun insertFoodItem(item: FoodItem): Long = dao.insertFoodItem(item)

    suspend fun updateFoodItem(item: FoodItem) = dao.updateFoodItem(item)

    fun getExpiredBatches(now: Long) = dao.getExpiredBatches(now)
    
    fun getNearExpiryBatches(now: Long, nearExpiry: Long) = dao.getNearExpiryBatches(now, nearExpiry)
    
    fun getTransactionHistory() = dao.getTransactionHistory()

    suspend fun stockIn(batch: Batch, reason: String) {
        db.withTransaction {
            val batchId = dao.insertBatch(batch).toInt()
            dao.insertTransaction(
                StockTransaction(
                    itemId = batch.itemId,
                    batchId = batchId,
                    type = TransactionType.STOCK_IN,
                    quantity = batch.quantity,
                    reason = reason
                )
            )
        }
    }
    
    suspend fun stockOutOrWastage(itemId: Int, quantity: Int, type: TransactionType, reason: String) {
        db.withTransaction {
            var remaining = quantity
            val now = System.currentTimeMillis()
            val batches = if (type == TransactionType.STOCK_OUT) {
                dao.getValidBatches(itemId, now)
            } else {
                dao.getAvailableBatchesForItem(itemId)
            }
            
            val available = batches.sumOf { it.quantity }
            if (available < quantity) throw Exception("Not enough stock available for this operation.")
            
            for (batch in batches) {
                if (remaining <= 0) break
                val take = minOf(batch.quantity, remaining)
                dao.updateBatch(batch.copy(quantity = batch.quantity - take))
                dao.insertTransaction(
                    StockTransaction(
                        itemId = itemId,
                        batchId = batch.batchId,
                        type = type,
                        quantity = take,
                        reason = reason
                    )
                )
                remaining -= take
            }
        }
    }
}