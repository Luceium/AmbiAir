package tech.luceium.ambiair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.biometry.BiometryAuthenticator
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ComposeViewModel(
    val biometryAuthenticator: BiometryAuthenticator
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun tryToAuth() {
        viewModelScope.launch {
            try {
                val isSuccess = biometryAuthenticator.checkBiometryAuthentication(
                    requestTitle = "Biometry".desc(),
                    requestReason = "Just for test".desc(),
                    failureButtonText = "Oops".desc(),
                    allowDeviceCredentials = false
                )

                if (isSuccess) {
                    _authState.value = AuthState.Success("Authentication successful")
                } else {
                    _authState.value = AuthState.Failure("Authentication failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Failure("Authentication error: ${e.message}")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    data class Success(val message: String) : AuthState()
    data class Failure(val error: String) : AuthState()
}
