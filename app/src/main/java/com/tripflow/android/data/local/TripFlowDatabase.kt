package com.tripflow.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tripflow.android.data.local.dao.ExpenseDao
import com.tripflow.android.data.local.dao.TripDao
import com.tripflow.android.data.local.dao.UserDao
import com.tripflow.android.data.local.entity.ExpenseEntity
import com.tripflow.android.data.local.entity.TripEntity
import com.tripflow.android.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, TripEntity::class, ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TripFlowDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: TripFlowDatabase? = null

        fun getDatabase(context: Context): TripFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TripFlowDatabase::class.java,
                    "tripflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
