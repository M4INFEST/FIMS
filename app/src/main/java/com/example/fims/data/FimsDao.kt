package com.example.fims.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FimsDao {
    @Query("SELECT * FROM food_items WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveFoodItems(): Flow<List<FoodItem>>
    
    @Query("""
        SELECT f.*, COALESCE(SUM(b.quantity), 0) AS totalStock 
        FROM food_items f
        LEFT JOIN batches b ON f.itemId = b.itemId
        WHERE f.isActive = 1
        GROUP BY f.itemId
        ORDER BY f.name ASC
    """)
    fun getItemsWithStock(): Flow<List<ItemWithStock>>
    
    @Query("SELECT * FROM food_items WHERE itemId = :id")
    suspend fun getFoodItemById(id: Int): FoodItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItem): Long

    @Update
    suspend fun updateFoodItem(item: FoodItem)
    
    @Query("SELECT * FROM batches WHERE itemId = :itemId AND quantity > 0 ORDER BY expiryDate ASC")
    suspend fun getAvailableBatchesForItem(itemId: Int): List<Batch>

    @Query("SELECT * FROM batches WHERE itemId = :itemId AND quantity > 0 AND expiryDate > :now ORDER BY expiryDate ASC")
    suspend fun getValidBatches(itemId: Int, now: Long): List<Batch>
    
    @Query("SELECT b.*, f.name as itemName, f.unit as itemUnit FROM batches b INNER JOIN food_items f ON b.itemId = f.itemId WHERE b.quantity > 0 AND b.expiryDate < :now ORDER BY b.expiryDate ASC")
    fun getExpiredBatches(now: Long): Flow<List<BatchInfo>>

    @Query("SELECT b.*, f.name as itemName, f.unit as itemUnit FROM batches b INNER JOIN food_items f ON b.itemId = f.itemId WHERE b.quantity > 0 AND b.expiryDate >= :now AND b.expiryDate <= :nearExpiryTime ORDER BY b.expiryDate ASC")
    fun getNearExpiryBatches(now: Long, nearExpiryTime: Long): Flow<List<BatchInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch): Long

    @Update
    suspend fun updateBatch(batch: Batch)

    @Insert
    suspend fun insertTransaction(transaction: StockTransaction)
    
    @Query("SELECT SUM(quantity) FROM batches WHERE itemId = :itemId")
    fun getTotalStockForItem(itemId: Int): Flow<Int?>
    
    @Query("SELECT t.*, f.name as itemName FROM transactions t INNER JOIN food_items f ON t.itemId = f.itemId ORDER BY t.createdAt DESC")
    fun getTransactionHistory(): Flow<List<TransactionHistoryItem>>
}