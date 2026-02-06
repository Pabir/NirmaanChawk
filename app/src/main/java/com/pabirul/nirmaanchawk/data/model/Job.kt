package com.pabirul.nirmaanchawk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Job(
    val id: String? = null,
    @SerialName("client_id")
    val clientId: String,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val budget: Double? = null,
    val status: String = "open",
    @SerialName("created_at")
    val createdAt: String? = null,
    val profiles: Profile? = null, // The job creator's profile
    @SerialName("job_applications")
    val applications: List<JobApplication> = emptyList() // Applicants for this job
)
