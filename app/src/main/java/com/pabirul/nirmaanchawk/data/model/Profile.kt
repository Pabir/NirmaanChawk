package com.pabirul.nirmaanchawk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val email: String? = null,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("role")
    val role: UserRole,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    val skills: List<String>? = null,
    @SerialName("daily_rate")
    val dailyRate: Double? = null,
    @SerialName("business_name")
    val businessName: String? = null
)
