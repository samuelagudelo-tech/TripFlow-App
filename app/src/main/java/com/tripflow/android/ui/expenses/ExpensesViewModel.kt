package com.tripflow.android.ui.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.android.data.local.TripFlowDatabase
import com.tripflow.android.data.local.entity.ExpenseEntity
import com.tripflow.android.data.local.entity.TripEntity
import com.tripflow.android.data.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TripFlowDatabase.getDatabase(application)
    private val expenseDao = database.expenseDao()
    private val tripDao = database.tripDao()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _trips = MutableStateFlow<List<TripEntity>>(emptyList())
    val trips: StateFlow<List<TripEntity>> = _trips.asStateFlow()

    init {
        viewModelScope.launch {
            expenseDao.getAllExpenses().collect { storedExpenses ->
                _expenses.value = storedExpenses.map { it.toModel() }
            }
        }
        viewModelScope.launch {
            tripDao.getAllTrips().collect { storedTrips ->
                _trips.value = storedTrips
            }
        }
    }

    fun addExpense(expense: Expense) {
        val tripId = expense.tripId.toLongOrNull() ?: return

        viewModelScope.launch {
            expenseDao.insertExpense(
                ExpenseEntity(
                    tripId = tripId,
                    title = expense.title,
                    amount = expense.amount,
                    category = expense.category,
                    date = expense.createdAt.ifBlank { System.currentTimeMillis().toString() }
                )
            )
        }
    }

    private fun ExpenseEntity.toModel() = Expense(
        id = id.toString(),
        tripId = tripId.toString(),
        title = title,
        amount = amount,
        category = category,
        createdAt = date
    )
}
