package com.example.weathermap.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathermap.R
import com.example.weathermap.domain.repository.AuthRepository
import com.example.weathermap.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessageRes: Int? = null,
    val customError: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isAuthenticated = authRepository.isUserAuthenticated) }
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _uiState.update { it.copy(isAuthenticated = user != null) }
            }
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessageRes = null, customError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessageRes = null, customError = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessageRes = null, customError = null) }
    }

    fun signIn(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_email_empty) }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_email_invalid) }
            return
        }
        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_password_empty) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null, customError = null) }
            val result = authRepository.signIn(email, password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        customError = result.exceptionOrNull()?.localizedMessage ?: "Sign in failed"
                    )
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_email_empty) }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_email_invalid) }
            return
        }
        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_password_empty) }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_password_short) }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_passwords_mismatch) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null, customError = null) }
            val result = authRepository.register(email, password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        customError = result.exceptionOrNull()?.localizedMessage ?: "Registration failed"
                    )
                }
            }
        }
    }

    fun signOut(onLoggedOut: () -> Unit) {
        authRepository.signOut()
        weatherRepository.clearCache()
        _uiState.update { AuthUiState() }
        onLoggedOut()
    }
}
