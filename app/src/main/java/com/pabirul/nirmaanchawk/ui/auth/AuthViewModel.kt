package com.pabirul.nirmaanchawk.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pabirul.nirmaanchawk.data.model.Profile
import com.pabirul.nirmaanchawk.data.model.UserRole
import com.pabirul.nirmaanchawk.data.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _sessionStatus = MutableStateFlow<SessionStatus>(SessionStatus.NotAuthenticated(isSignOut = false))
    val sessionStatus: StateFlow<SessionStatus> = _sessionStatus.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSessionStatus().collect {
                _sessionStatus.value = it
                if (it is SessionStatus.Authenticated) {
                    checkProfile()
                } else if (it is SessionStatus.NotAuthenticated) {
                    _uiState.value = AuthState.Idle
                }
            }
        }
    }

    private fun checkProfile() {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            val profile = repository.getCurrentProfile()
            if (profile == null || profile.fullName.isBlank()) {
                _uiState.value = AuthState.NeedsRegistration
            } else {
                _uiState.value = AuthState.Authenticated(profile)
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                repository.signInWithEmail(email, password)
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                repository.signUpWithEmail(email, password)
                // After a successful sign-up, the user is authenticated but needs to register a profile.
                _uiState.value = AuthState.NeedsRegistration
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signInWithOtp(phone: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                repository.signInWithOtp(phone)
                _uiState.value = AuthState.OtpSent
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Failed to send OTP")
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                repository.verifyOtp(phone, otp)
                // After a successful OTP verification, the user is authenticated but needs to register a profile.
                _uiState.value = AuthState.NeedsRegistration
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "OTP verification failed")
            }
        }
    }

    fun registerProfile(
        fullName: String,
        role: UserRole,
        phone: String,
        skills: List<String>? = null,
        dailyRate: Double? = null,
        businessName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val authenticated = sessionStatus.value as? SessionStatus.Authenticated
                val user = authenticated?.session?.user
                if (user != null) {
                    val profile = Profile(
                        id = user.id,
                        email = user.email,
                        fullName = fullName,
                        role = role,
                        phoneNumber = phone,
                        skills = skills,
                        dailyRate = dailyRate,
                        businessName = businessName
                    )
                    repository.updateProfile(profile)
                    _uiState.value = AuthState.Authenticated(profile)
                }
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
                _uiState.value = AuthState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Sign out failed")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    object NeedsRegistration : AuthState()
    data class Authenticated(val profile: Profile) : AuthState()
    data class Error(val message: String) : AuthState()
}
