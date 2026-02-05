package com.pabirul.nirmaanchawk.data.repository

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
        
        return postgrest["jobs"]
            .select(Columns.raw("*, profiles(*)")) {
                if (role == UserRole.CLIENT || role == UserRole.CONTRACTOR) {
                    if (userId != null) {
                        filter {
                            eq("client_id", userId)
                        }
                    } else {
                        // If no user is found but they aren't a laborer, show nothing
                        filter {
                            eq("client_id", "00000000-0000-0000-0000-000000000000") 
                        }
                    }
                }
                // LABORER role doesn't get a filter, so they see all jobs.
            }
            .decodeList()
    }

    suspend fun postJob(job: Job) {
        val jobData = job.copy(profiles = null)
        postgrest["jobs"].insert(jobData)
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
