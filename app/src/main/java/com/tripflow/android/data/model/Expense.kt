package com.tripflow.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String = "",
    @SerialName("trip_id") val tripId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Alimentación",
    @SerialName("receipt_url") val receiptUrl: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    val trips: Trip? = null
)
