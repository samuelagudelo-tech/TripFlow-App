package com.tripflow.android.data.local.dao

import androidx.room.*
import com.tripflow.android.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY date DESC")
    fun getExpensesByTrip(tripId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT receipt_image FROM expenses WHERE id = :expenseId")
    suspend fun getReceiptImage(expenseId: Long): ByteArray?
}
