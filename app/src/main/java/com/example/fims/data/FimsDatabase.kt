package com.example.fims.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FoodItem::class, Batch::class, StockTransaction::class], version = 1, exportSchema = false)
abstract class FimsDatabase : RoomDatabase() {

    abstract fun fimsDao(): FimsDao

    companion object {
        @Volatile
        private var INSTANCE: FimsDatabase? = null

        fun getDatabase(context: Context): FimsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FimsDatabase::class.java,
                    "fims_database"
                )
                .addCallback(FimsDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FimsDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.fimsDao())
                }
            }
        }

        suspend fun populateDatabase(dao: FimsDao) {
            // Seed items
            val tomato = FoodItem(name = "Tomatoes", category = "Vegetables", unit = "kg", reorderLevel = 10)
            val milk = FoodItem(name = "Milk", category = "Dairy", unit = "Liters", reorderLevel = 5)
            val rice = FoodItem(name = "Basmati Rice", category = "Grains", unit = "kg", reorderLevel = 20)
            
            val tomatoId = dao.insertFoodItem(tomato).toInt()
            val milkId = dao.insertFoodItem(milk).toInt()
            val riceId = dao.insertFoodItem(rice).toInt()

            val now = System.currentTimeMillis()
            val oneDay = 24L * 60 * 60 * 1000

            // Seed batches
            val b1 = Batch(itemId = tomatoId, batchNo = "B-001", expiryDate = now + (3 * oneDay), quantity = 15, unitCost = 2.5, receivedDate = now - (2 * oneDay))
            val b2 = Batch(itemId = milkId, batchNo = "B-002", expiryDate = now + (1 * oneDay), quantity = 6, unitCost = 1.5, receivedDate = now - (1 * oneDay))
            val b3 = Batch(itemId = riceId, batchNo = "B-003", expiryDate = now + (30 * oneDay), quantity = 50, unitCost = 4.0, receivedDate = now)

            val b1Id = dao.insertBatch(b1).toInt()
            val b2Id = dao.insertBatch(b2).toInt()
            val b3Id = dao.insertBatch(b3).toInt()

            // Seed transactions
            dao.insertTransaction(StockTransaction(itemId = tomatoId, batchId = b1Id, type = TransactionType.STOCK_IN, quantity = 15, reason = "Initial Stock", createdAt = now - (2 * oneDay)))
            dao.insertTransaction(StockTransaction(itemId = milkId, batchId = b2Id, type = TransactionType.STOCK_IN, quantity = 6, reason = "Initial Stock", createdAt = now - (1 * oneDay)))
            dao.insertTransaction(StockTransaction(itemId = riceId, batchId = b3Id, type = TransactionType.STOCK_IN, quantity = 50, reason = "Initial Stock", createdAt = now))
        }
    }
}