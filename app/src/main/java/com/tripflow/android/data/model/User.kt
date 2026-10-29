package com.tripflow.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val email: String = "",
    @SerialName("full_name") val fullName: String = "",
    val phone: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)
