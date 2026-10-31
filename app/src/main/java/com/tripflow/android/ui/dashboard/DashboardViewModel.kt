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
                // Consultamos los viajes desde Room (userId = 1 por defecto en sesión local)
                tripDao.getTripsByUser(1L).collect { trips ->
                    val activeTrip = selectActiveOrUpcomingTrip(trips)

                    if (activeTrip != null) {
                        expenseDao.getExpensesByTrip(activeTrip.id).collect { expenses ->
                            processTripAndExpenses(activeTrip, expenses)
                        }
                    } else {
                        // Si no hay viajes en Room, podemos cargar datos de demostración o dejar estado vacío
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
            } catch (_: Exception) {
                loadDemoData()
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

    fun insertDemoDataIfEmpty() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val demoTrip = TripEntity(
                    userId = 1L,
                    destination = "Madrid, España",
                    startDate = today.minusDays(2).toString(),
                    endDate = today.plusDays(5).toString(),
                    budgetLimit = 3000.0,
                    currency = "USD"
                )
                val tripId = tripDao.insertTrip(demoTrip)

                val demoExpenses = listOf(
                    ExpenseEntity(
                        tripId = tripId,
                        title = "Cena de bienvenida",
                        amount = 85.0,
                        category = "Alimentación",
                        date = today.minusDays(2).toString()
                    ),
                    ExpenseEntity(
                        tripId = tripId,
                        title = "Boleto Metro 10 viajes",
                        amount = 24.50,
                        category = "Transporte",
                        date = today.minusDays(1).toString()
                    ),
                    ExpenseEntity(
                        tripId = tripId,
                        title = "Hotel Gran Vía (Noches 1-3)",
                        amount = 620.0,
                        category = "Alojamiento",
                        date = today.minusDays(1).toString()
                    ),
                    ExpenseEntity(
                        tripId = tripId,
                        title = "Entrada Museo del Prado",
                        amount = 30.0,
                        category = "Entretenimiento",
                        date = today.toString()
                    ),
                    ExpenseEntity(
                        tripId = tripId,
                        title = "Almuerzo tapas Plaza Mayor",
                        amount = 45.0,
                        category = "Alimentación",
                        date = today.toString()
                    ),
                    ExpenseEntity(
                        tripId = tripId,
                        title = "Vuelo conexión Madrid",
                        amount = 445.50,
                        category = "Vuelos",
                        date = today.minusDays(2).toString()
                    )
                )

                for (exp in demoExpenses) {
                    expenseDao.insertExpense(exp)
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadDemoData() {
        val today = LocalDate.now()
        val startDate = today.minusDays(2).toString()
        val endDate = today.plusDays(5).toString()

        val mockTrip = TripEntity(
            id = 1L,
            userId = 1L,
            destination = "Madrid, España",
            startDate = startDate,
            endDate = endDate,
            budgetLimit = 3000.0,
            currency = "USD"
        )

        val mockExpenses = listOf(
            ExpenseEntity(
                id = 1L,
                tripId = mockTrip.id,
                title = "Cena de bienvenida",
                amount = 85.0,
                category = "Alimentación",
                date = today.minusDays(2).toString()
            ),
            ExpenseEntity(
                id = 2L,
                tripId = mockTrip.id,
                title = "Boleto Metro 10 viajes",
                amount = 24.50,
                category = "Transporte",
                date = today.minusDays(1).toString()
            ),
            ExpenseEntity(
                id = 3L,
                tripId = mockTrip.id,
                title = "Hotel Gran Vía (Noches 1-3)",
                amount = 620.0,
                category = "Alojamiento",
                date = today.minusDays(1).toString()
            ),
            ExpenseEntity(
                id = 4L,
                tripId = mockTrip.id,
                title = "Entrada Museo del Prado",
                amount = 30.0,
                category = "Entretenimiento",
                date = today.toString()
            ),
            ExpenseEntity(
                id = 5L,
                tripId = mockTrip.id,
                title = "Almuerzo tapas Plaza Mayor",
                amount = 45.0,
                category = "Alimentación",
                date = today.toString()
            ),
            ExpenseEntity(
                id = 6L,
                tripId = mockTrip.id,
                title = "Vuelo conexión Madrid",
                amount = 445.50,
                category = "Vuelos",
                date = today.minusDays(2).toString()
            )
        )

        val totalSpent = mockExpenses.sumOf { it.amount }
        val daysRemaining = calculateDaysRemaining(endDate)
        val stats = calculateDailyStats(mockExpenses, _uiState.value.selectedTimeRange)

        _uiState.update {
            it.copy(
                isLoading = false,
                activeTrip = mockTrip,
                activeTripExpenses = mockExpenses,
                totalSpent = totalSpent,
                budgetLimit = mockTrip.budgetLimit,
                currency = mockTrip.currency,
                daysRemaining = daysRemaining,
                isOverBudget = totalSpent > mockTrip.budgetLimit,
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
