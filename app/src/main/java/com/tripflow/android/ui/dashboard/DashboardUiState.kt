package com.tripflow.android.ui.dashboard

import com.tripflow.android.data.local.entity.ExpenseEntity
import com.tripflow.android.data.local.entity.TripEntity

enum class TimeRangeFilter {
    WEEK,
    MONTH,
}

data class DailyExpenseStat(
    val dayLabel: String,
    val amount: Double,
    val dateString: String,
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val activeTrip: TripEntity? = null,
    val activeTripExpenses: List<ExpenseEntity> = emptyList(),
    val totalSpent: Double = 0.0,
    val budgetLimit: Double = 0.0,
    val currency: String = "USD",
    val daysRemaining: Long = 0,
    val isOverBudget: Boolean = false,
    val selectedTimeRange: TimeRangeFilter = TimeRangeFilter.WEEK,
    val dailyStats: List<DailyExpenseStat> = emptyList(),
    val errorMessage: String? = null,
)
