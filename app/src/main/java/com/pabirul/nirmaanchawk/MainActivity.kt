package com.pabirul.nirmaanchawk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pabirul.nirmaanchawk.ui.auth.AuthState
import com.pabirul.nirmaanchawk.ui.auth.AuthViewModel
import com.pabirul.nirmaanchawk.ui.auth.LoginScreen
import com.pabirul.nirmaanchawk.ui.auth.RegistrationScreen
import com.pabirul.nirmaanchawk.ui.jobs.JobBoardScreen
import com.pabirul.nirmaanchawk.ui.jobs.PostJobScreen
import com.pabirul.nirmaanchawk.ui.theme.NirmaanChawkTheme
import io.github.jan.supabase.auth.status.SessionStatus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NirmaanChawkTheme {
                val authViewModel: AuthViewModel = viewModel()
                val uiState by authViewModel.uiState.collectAsState()
                val sessionStatus by authViewModel.sessionStatus.collectAsState()
                
                // Keep track of whether we are in the "Post Job" screen
                var isPostingJob by remember { mutableStateOf(false) }

                // Reset navigation when user logs out
                LaunchedEffect(uiState) {
                    if (uiState !is AuthState.Authenticated) {
                        isPostingJob = false
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (val state = uiState) {
                            is AuthState.Idle, is AuthState.Loading, is AuthState.OtpSent, is AuthState.Error -> {
                                LoginScreen(viewModel = authViewModel) {}
                            }
                            is AuthState.NeedsRegistration -> {
                                val phone = (sessionStatus as? SessionStatus.Authenticated)?.session?.user?.phone ?: ""
                                RegistrationScreen(phone = phone, viewModel = authViewModel)
                            }
                            is AuthState.Authenticated -> {
                                if (isPostingJob) {
                                    PostJobScreen(
                                        onBack = { isPostingJob = false }
                                    )
                                } else {
                                    JobBoardScreen(
                                        profile = state.profile,
                                        role = state.profile.role,
                                        onSignOut = { authViewModel.signOut() },
                                        onPostJobClicked = { isPostingJob = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
