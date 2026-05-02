package com.kal.portfolio.pinninglab.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kal.portfolio.pinninglab.data.model.HttpBinResponse
import com.kal.portfolio.pinninglab.data.repository.NetworkRepository
import com.kal.portfolio.pinninglab.data.repository.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NetworkUiState(
    val result: HttpBinResponse? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val lastAction: String? = null
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    fun get() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, lastAction = "GET /get") }
            when (val result = repository.get()) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoading = false, result = result.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message, result = null)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun headers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, lastAction = "GET /headers") }
            when (val result = repository.headers()) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoading = false, result = result.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message, result = null)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}