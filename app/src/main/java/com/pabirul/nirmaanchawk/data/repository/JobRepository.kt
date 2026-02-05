package com.pabirul.nirmaanchawk.data.repository

import android.util.Log
import com.pabirul.nirmaanchawk.data.model.Job
import com.pabirul.nirmaanchawk.data.model.UserRole
import com.pabirul.nirmaanchawk.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.Columns

class JobRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    suspend fun getJobs(role: UserRole): List<Job> {
        val userId = auth.currentUserOrNull()?.id
        Log.d("JobRepository", "Fetching jobs for role: $role, userId: $userId")
        
        return try {
            val jobs = postgrest["jobs"]
                .select(Columns.raw("*, profiles(*)")) {
                    filter {
                        if (role == UserRole.LABORER) {
                            eq("status", "open")
                        } else if (role == UserRole.CLIENT || role == UserRole.CONTRACTOR) {
                            if (userId != null) {
                                eq("client_id", userId)
                            } else {
                                eq("client_id", "00000000-0000-0000-0000-000000000000") 
                            }
                        }
                    }
                }
                .decodeList<Job>()
            Log.d("JobRepository", "Fetched ${jobs.size} jobs")
            jobs
        } catch (e: Exception) {
            Log.e("JobRepository", "Error fetching jobs", e)
            throw e
        }
    }

    suspend fun postJob(job: Job) {
        val jobData = job.copy(profiles = null)
        postgrest["jobs"].insert(jobData)
    }

    suspend fun updateJobStatus(jobId: String, status: String) {
        Log.d("JobRepository", "Updating job status: $jobId to $status")
        try {
            postgrest["jobs"].update({
                set("status", status)
            }) {
                filter {
                    eq("id", jobId)
                }
            }
            Log.d("JobRepository", "Job status updated successfully")
        } catch (e: Exception) {
            Log.e("JobRepository", "Error updating job status", e)
            throw e
        }
    }

    suspend fun getMyJobs(): List<Job> {
        val userId = auth.currentUserOrNull()?.id ?: return emptyList()
        return postgrest["jobs"].select(Columns.raw("*, profiles(*)")) {
            filter {
                eq("client_id", userId)
            }
        }.decodeList()
    }
}
