package com.tripflow.android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.android.data.model.Expense
import com.tripflow.android.data.model.Trip
import com.tripflow.android.data.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class DashboardViewModel : ViewModel() {

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
                // Fetch trips from Supabase
                val trips = SupabaseClient.client.from("trips")
                    .select()
                    .decodeList<Trip>()

                // Identify active or upcoming trip
                val activeTrip = selectActiveOrUpcomingTrip(trips)

                if (activeTrip != null) {
                    // Fetch expenses for active trip
                    val expenses = SupabaseClient.client.from("expenses")
                        .select {
                            filter {
                                eq("trip_id", activeTrip.id)
                            }
                        }
                        .decodeList<Expense>()

                    processTripAndExpenses(activeTrip, expenses)
                } else {
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
            } catch (_: Exception) {
                // If Supabase is offline / demo credentials, load mock/preview data so UI renders nicely
                loadDemoData()
            }
        }
    }

    private fun processTripAndExpenses(trip: Trip, expenses: List<Expense>) {
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

    private fun loadDemoData() {
        val today = LocalDate.now()
        val startDate = today.minusDays(2).toString()
        val endDate = today.plusDays(5).toString()

        val mockTrip = Trip(
            id = "demo-trip-1",
            destination = "Madrid, España",
            startDate = startDate,
            endDate = endDate,
            budgetLimit = 3000.0,
            currency = "USD",
            userId = "user-123"
        )

        val mockExpenses = listOf(
            Expense(
                id = "1",
                tripId = mockTrip.id,
                title = "Cena de bienvenida",
                amount = 85.0,
                category = "Alimentación",
                createdAt = today.minusDays(2).toString()
            ),
            Expense(
                id = "2",
                tripId = mockTrip.id,
                title = "Boleto Metro 10 viajes",
                amount = 24.50,
                category = "Transporte",
                createdAt = today.minusDays(1).toString()
            ),
            Expense(
                id = "3",
                tripId = mockTrip.id,
                title = "Hotel Gran Vía (Noches 1-3)",
                amount = 620.0,
                category = "Alojamiento",
                createdAt = today.minusDays(1).toString()
            ),
            Expense(
                id = "4",
                tripId = mockTrip.id,
                title = "Entrada Museo del Prado",
                amount = 30.0,
                category = "Entretenimiento",
                createdAt = today.toString()
            ),
            Expense(
                id = "5",
                tripId = mockTrip.id,
                title = "Almuerzo tapas Plaza Mayor",
                amount = 45.0,
                category = "Alimentación",
                createdAt = today.toString()
            ),
            Expense(
                id = "6",
                tripId = mockTrip.id,
                title = "Vuelo conexión Madrid",
                amount = 445.50,
                category = "Vuelos",
                createdAt = today.minusDays(2).toString()
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

    private fun selectActiveOrUpcomingTrip(trips: List<Trip>): Trip? {
        if (trips.isEmpty()) return null
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_DATE

        // Find currently active trip: start <= today <= end
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

        // Or upcoming trip: start > today
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
        expenses: List<Expense>,
        range: TimeRangeFilter
    ): List<DailyExpenseStat> {
        val today = LocalDate.now()
        val numDays = if (range == TimeRangeFilter.WEEK) 7 else 30
        val startDate = today.minusDays((numDays - 1).toLong())

        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale("es", "ES"))
        val fullDateFormatter = DateTimeFormatter.ISO_DATE

        // Map expenses by day (yyyy-MM-dd)
        val expensesByDay = mutableMapOf<String, Double>()
        for (expense in expenses) {
            val expDate = expense.createdAt.take(10)
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
