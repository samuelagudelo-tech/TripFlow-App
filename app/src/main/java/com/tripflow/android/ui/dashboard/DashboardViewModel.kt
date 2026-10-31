package com.tripflow.android.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.android.data.local.TripFlowDatabase
import com.tripflow.android.data.local.entity.ExpenseEntity
import com.tripflow.android.data.local.entity.TripEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripFlowDatabase.getDatabase(application)
    private val tripDao = database.tripDao()
    private val expenseDao = database.expenseDao()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun setTimeRange(filter: TimeRangeFilter) {
        _uiState.update { current ->
            val updatedStats = calculateDailyStats(current.activeTripExpenses, filter)
            current.copy(selectedTimeRange = filter, dailyStats = updatedStats)
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Consultamos exclusivamente los viajes reales desde Room (userId = 1)
                tripDao.getTripsByUser(1L).collect { trips ->
                    val activeTrip = selectActiveOrUpcomingTrip(trips)

                    if (activeTrip != null) {
                        expenseDao.getExpensesByTrip(activeTrip.id).collect { expenses ->
                            processTripAndExpenses(activeTrip, expenses)
                        }
                    } else {
                        // Estado vacío: no hay viajes reales en la base de datos
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                activeTrip = null,
                                activeTripExpenses = emptyList(),
                                totalSpent = 0.0,
                                budgetLimit = 0.0,
                                dailyStats = emptyList(),
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Error al cargar datos",
                        activeTrip = null,
                        activeTripExpenses = emptyList(),
                        totalSpent = 0.0,
                        budgetLimit = 0.0,
                        dailyStats = emptyList(),
                    )
                }
            }
        }
    }

    private fun processTripAndExpenses(trip: TripEntity, expenses: List<ExpenseEntity>) {
        val totalSpent = expenses.sumOf { it.amount }
        val budgetLimit = trip.budgetLimit
        val daysRemaining = calculateDaysRemaining(trip.endDate)
        val isOver = (totalSpent > budgetLimit) && (budgetLimit > 0)
        val stats = calculateDailyStats(expenses, _uiState.value.selectedTimeRange)

        _uiState.update {
            it.copy(
                isLoading = false,
                activeTrip = trip,
                activeTripExpenses = expenses,
                totalSpent = totalSpent,
                budgetLimit = budgetLimit,
                currency = trip.currency.ifBlank { "USD" },
                daysRemaining = daysRemaining,
                isOverBudget = isOver,
                dailyStats = stats,
                errorMessage = null,
            )
        }
    }

    private fun selectActiveOrUpcomingTrip(trips: List<TripEntity>): TripEntity? {
        if (trips.isEmpty()) return null
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_DATE

        // Buscar viaje activo: start <= today <= end
        val active = trips.firstOrNull { trip ->
            try {
                val start = LocalDate.parse(trip.startDate.take(10), formatter)
                val end = LocalDate.parse(trip.endDate.take(10), formatter)
                !today.isBefore(start) && !today.isAfter(end)
            } catch (_: Exception) {
                false
            }
        }
        if (active != null) return active

        // O viaje próximo: start > today
        val upcoming = trips.asSequence().filter { trip ->
            try {
                val start = LocalDate.parse(trip.startDate.take(10), formatter)
                today.isBefore(start)
            } catch (_: Exception) {
                false
            }
        }.minByOrNull { trip ->
            LocalDate.parse(trip.startDate.take(10), formatter)
        }

        return upcoming ?: trips.firstOrNull()
    }

    private fun calculateDaysRemaining(endDateStr: String): Long {
        return try {
            val end = LocalDate.parse(endDateStr.take(10), DateTimeFormatter.ISO_DATE)
            val today = LocalDate.now()
            val diff = ChronoUnit.DAYS.between(today, end)
            if (diff >= 0) diff else 0
        } catch (_: Exception) {
            0
        }
    }

    private fun calculateDailyStats(
        expenses: List<ExpenseEntity>,
        range: TimeRangeFilter
    ): List<DailyExpenseStat> {
        val today = LocalDate.now()
        val numDays = if (range == TimeRangeFilter.WEEK) 7 else 30
        val startDate = today.minusDays((numDays - 1).toLong())

        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale("es", "ES"))
        val fullDateFormatter = DateTimeFormatter.ISO_DATE

        val expensesByDay = mutableMapOf<String, Double>()
        for (expense in expenses) {
            val expDate = expense.date.take(10)
            if (expDate.isNotBlank()) {
                expensesByDay[expDate] = (expensesByDay[expDate] ?: 0.0) + expense.amount
            }
        }

        val result = mutableListOf<DailyExpenseStat>()
        for (i in 0 until numDays) {
            val date = startDate.plusDays(i.toLong())
            val dateKey = date.format(fullDateFormatter)
            val label = if (range == TimeRangeFilter.WEEK) {
                date.format(dayFormatter).replaceFirstChar { it.uppercase() }
            } else {
                date.dayOfMonth.toString()
            }
            val amount = expensesByDay[dateKey] ?: 0.0
            result.add(
                DailyExpenseStat(
                    dayLabel = label,
                    amount = amount,
                    dateString = dateKey,
                )
            )
        }
        return result
    }
}
