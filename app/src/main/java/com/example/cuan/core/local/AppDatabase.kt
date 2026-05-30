package com.example.cuan.core.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cuan.core.local.dao.SavingsGoalDao
import com.example.cuan.core.local.dao.TransactionQueueDao
import com.example.cuan.core.local.entity.OfflineTransactionEntity
import com.example.cuan.core.local.entity.SavingsGoalEntity

// Room Database for CuOne app 
@Database(
    entities = [
        OfflineTransactionEntity::class,
        SavingsGoalEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionQueueDao(): TransactionQueueDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "c_one_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
