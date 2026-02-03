package com.pabirul.nirmaanchawk.data.repository

import com.pabirul.nirmaanchawk.data.model.Job
import com.pabirul.nirmaanchawk.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.Columns

class JobRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    suspend fun getJobs(): List<Job> {
        // Fetch jobs and join with the profiles table using the foreign key relationship
        return postgrest["jobs"]
            .select(Columns.raw("*, profiles(*)"))
            .decodeList()
    }

    suspend fun postJob(job: Job) {
        // Remove the profiles property before inserting to avoid Postgrest errors
        // since 'profiles' is a joined property, not a column in the jobs table.
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
