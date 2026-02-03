package com.pabirul.nirmaanchawk.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pabirul.nirmaanchawk.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    phone: String,
    viewModel: AuthViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.LABORER) }
    var expanded by remember { mutableStateOf(false) }

    // Role specific fields
    var skills by remember { mutableStateOf("") }
    var dailyRate by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Complete Your Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedRole.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("I am a...") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                UserRole.entries.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.name) },
                        onClick = {
                            selectedRole = role
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (selectedRole) {
            UserRole.LABORER -> {
                TextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Primary Skills (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = dailyRate,
                    onValueChange = { dailyRate = it },
                    label = { Text("Daily Wage Expectation (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            UserRole.CONTRACTOR -> {
                TextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Specialization") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            UserRole.CLIENT -> {
                // Clients might just need basic info for now as per plan
                Text(text = "Registering as a Client to hire workers.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val uiState by viewModel.uiState.collectAsState()

        Button(
            onClick = {
                viewModel.registerProfile(
                    fullName = fullName,
                    role = selectedRole,
                    phone = phone,
                    skills = if (skills.isNotBlank()) skills.split(",").map { it.trim() } else null,
                    dailyRate = dailyRate.toDoubleOrNull(),
                    businessName = if (businessName.isNotBlank()) businessName else null
                )
            },
            enabled = fullName.isNotBlank() && uiState !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Complete Registration")
            }
        }

        if (uiState is AuthState.Error) {
            Text(
                text = (uiState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
