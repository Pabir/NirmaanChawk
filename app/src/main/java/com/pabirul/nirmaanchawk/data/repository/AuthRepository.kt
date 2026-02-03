package com.pabirul.nirmaanchawk.data.repository

import com.pabirul.nirmaanchawk.data.model.Profile
import com.pabirul.nirmaanchawk.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository {
    private val auth = SupabaseClient.client.auth
    private val postgrest = SupabaseClient.client.postgrest

    suspend fun signUpWithEmail(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signInWithOtp(phone: String) {
        auth.signInWith(OTP) {
            this.phone = phone
        }
    }

    suspend fun verifyOtp(phone: String, otp: String) {
        auth.verifyPhoneOtp(
            type = OtpType.Phone.SMS,
            phone = phone,
            token = otp
        )
    }

    suspend fun getCurrentProfile(): Profile? {
        val user = auth.currentUserOrNull() ?: return null
        return try {
            postgrest["profiles"]
                .select {
                    filter {
                        eq("id", user.id)
                    }
                }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Updates or inserts the user's profile. Using upsert ensures that the row is created
     * even if the database trigger (handle_new_user) didn't run or hasn't finished.
     */
    suspend fun updateProfile(profile: Profile) {
        postgrest["profiles"].upsert(profile)
    }

    fun getSessionStatus() = auth.sessionStatus

    suspend fun signOut() {
        auth.signOut()
    }
}
