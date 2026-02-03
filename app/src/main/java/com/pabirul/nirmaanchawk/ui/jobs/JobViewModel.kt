package com.pabirul.nirmaanchawk.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pabirul.nirmaanchawk.data.model.Job
import com.pabirul.nirmaanchawk.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JobViewModel(private val repository: JobRepository = JobRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<JobUiState>(JobUiState.Loading)
    val uiState: StateFlow<JobUiState> = _uiState.asStateFlow()

    fun getJobs() {
        viewModelScope.launch {
            _uiState.value = JobUiState.Loading
            try {
                _uiState.value = JobUiState.Success(repository.getJobs())
            } catch (e: Exception) {
                _uiState.value = JobUiState.Error(e.message ?: "Failed to load jobs")
            }
        }
    }

    fun getMyJobs() {
        viewModelScope.launch {
            _uiState.value = JobUiState.Loading
            try {
                _uiState.value = JobUiState.Success(repository.getMyJobs())
            } catch (e: Exception) {
                _uiState.value = JobUiState.Error(e.message ?: "Failed to load your jobs")
            }
        }
    }

    fun postJob(job: Job) {
        viewModelScope.launch {
            try {
                repository.postJob(job)
                // Refresh the list of jobs after posting
                getJobs()
            } catch (e: Exception) {
                _uiState.value = JobUiState.Error(e.message ?: "Failed to post job")
            }
        }
    }
}

sealed class JobUiState {
    object Loading : JobUiState()
    data class Success(val jobs: List<Job>) : JobUiState()
    data class Error(val message: String) : JobUiState()
}
