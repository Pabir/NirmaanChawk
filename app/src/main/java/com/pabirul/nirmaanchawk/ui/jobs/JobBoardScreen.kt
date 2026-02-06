package com.pabirul.nirmaanchawk.ui.jobs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pabirul.nirmaanchawk.data.model.Job
import com.pabirul.nirmaanchawk.data.model.Profile
import com.pabirul.nirmaanchawk.data.model.UserRole
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobBoardScreen(
    profile: Profile,
    role: UserRole,
    onSignOut: () -> Unit,
    onPostJobClicked: () -> Unit,
    viewModel: JobViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getJobs(role)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NirmaanChawk") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Logout", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (role == UserRole.CLIENT || role == UserRole.CONTRACTOR) {
                FloatingActionButton(onClick = onPostJobClicked) {
                    Icon(Icons.Default.Add, contentDescription = "Post Job")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            UserInfoHeader(profile = profile)
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is JobUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is JobUiState.Success -> {
                        if (state.jobs.isEmpty()) {
                            Text(
                                text = if (role == UserRole.LABORER) "No open jobs available." else "You haven\'t posted any jobs yet.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = if (role == UserRole.LABORER) "Available Jobs" else "My Posted Jobs",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(state.jobs) { job ->
                                    JobItem(
                                        job = job,
                                        role = role,
                                        currentUserId = profile.id,
                                        onToggleStatus = {
                                            viewModel.toggleJobStatus(job, role)
                                        },
                                        onApplyClick = {
                                            job.id?.let { viewModel.applyForJob(it, role) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is JobUiState.Error -> {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoHeader(profile: Profile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = profile.role.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                if (!profile.businessName.isNullOrBlank()) {
                    Text(
                        text = profile.businessName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                when (profile.role) {
                    UserRole.LABORER -> {
                        profile.skills?.let {
                            if (it.isNotEmpty()) {
                                Text(
                                    text = "Skills: ${it.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        profile.dailyRate?.let {
                            Text(
                                text = "Expected Wage: ₹$it/day",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    UserRole.CONTRACTOR -> {
                        profile.skills?.let {
                            if (it.isNotEmpty()) {
                                Text(
                                    text = "Specialization: ${it.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    UserRole.CLIENT -> {
                        // Clients might not have specific skills or rates to show here
                    }
                }
            }
        }
    }
}

@Composable
fun JobItem(
    job: Job,
    role: UserRole,
    currentUserId: String,
    onToggleStatus: () -> Unit,
    onApplyClick: () -> Unit
) {
    val isCompleted = job.status == "completed"
    val hasApplied = job.applications.any { it.applicant_id == currentUserId }
    var showApplicants by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFFF5F5F5) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Job Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = job.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = if (isCompleted) Color.LightGray else MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = job.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = job.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = job.description, style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Contractor/Client Info Section
            job.profiles?.let { profile ->
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Posted by: ${profile.fullName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!profile.businessName.isNullOrBlank()) {
                            Text(
                                text = profile.businessName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Applicants Section for Creator
            if (role != UserRole.LABORER && job.applications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showApplicants = !showApplicants }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Applicants (${job.applications.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        if (showApplicants) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = showApplicants) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        job.applications.forEach { application ->
                            application.profiles?.let { applicant ->
                                ApplicantDetailItem(applicant = applicant, status = application.status)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                job.budget?.let {
                    Text(
                        text = "Budget: ₹$it",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (role == UserRole.LABORER) {
                    Button(
                        onClick = onApplyClick,
                        modifier = Modifier.height(36.dp),
                        enabled = !hasApplied,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasApplied) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (hasApplied) "Applied" else "Apply Now")
                    }
                } else if (role == UserRole.CLIENT || role == UserRole.CONTRACTOR) {
                    Button(
                        onClick = onToggleStatus,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Color.Red else Color(0xFF4CAF50)
                        )
                    ) {
                        Text(if (isCompleted) "Completed" else "Complete")
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicantDetailItem(applicant: Profile, status: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = applicant.fullName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = status.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            if (!applicant.phoneNumber.isNullOrBlank()) {
                Text(
                    text = "Phone: ${applicant.phoneNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (!applicant.skills.isNullOrEmpty()) {
                Text(
                    text = "Skills: ${applicant.skills.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (applicant.dailyRate != null) {
                Text(
                    text = "Daily Rate: ₹${applicant.dailyRate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
