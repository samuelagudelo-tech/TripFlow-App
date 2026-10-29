package com.tripflow.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String = "",
    val destination: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("budget_limit") val budgetLimit: Double = 0.0,
    val currency: String = "USD",
    @SerialName("user_id") val userId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val expenses: List<Expense> = emptyList()
)
