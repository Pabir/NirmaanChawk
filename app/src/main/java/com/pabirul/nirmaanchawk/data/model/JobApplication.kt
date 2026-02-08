package com.pabirul.nirmaanchawk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobApplication(
    val id: String? = null,
    @SerialName("job_id")
    val jobId: String,
    @SerialName("applicant_id")
    val applicant_id: String,
    val status: String = "pending",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    val profiles: Profile? = null
)
