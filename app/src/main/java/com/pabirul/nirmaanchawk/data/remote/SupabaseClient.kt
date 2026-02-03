package com.pabirul.nirmaanchawk.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    private const val SUPABASE_URL = "https://spenhnwjbnyxihisemff.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNwZW5obndqYm55eGloaXNlbWZmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njk5NTE5MTgsImV4cCI6MjA4NTUyNzkxOH0.3L3akGViBbCUhqv77O7jkclnEkNswgmcyPJXfzmMITg"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
