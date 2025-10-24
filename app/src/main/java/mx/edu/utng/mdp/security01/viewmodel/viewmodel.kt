package mx.edu.utng.mdp.security01.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.edu.utng.mdp.security01.models.AuthState
import mx.edu.utng.mdp.security01.models.User
import mx.edu.utng.mdp.security01.repository.AuthRepository

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    // LiveData para observar cambios en el estado de autenticación
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    // LiveData para el usuario actual
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    /**
     * Inicializamos el ViewModel verificando si hay sesión activa
     */
    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            if (repository.isLoggedIn()) {
                val user = repository.getCurrentUser()
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)

                    // Validamos el token con el servidor
                    validateToken()
                }
            }
        }
    }

    fun login(email: String, password: String) {
        // Cambiamos el estado a Loading
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            // Llamamos al repositorio de forma asíncrona
            val result = repository.login(email, password)

            // Procesamos el resultado
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(
                    exception.message ?: "Error desconocido en el login"
                )
            }
        }
    }

    fun validateToken() {
        viewModelScope.launch {
            val result = repository.validateToken()

            result.onSuccess { isValid ->
                if (!isValid) {
                    // Token inválido o expirado
                    logout()
                }
            }.onFailure {
                // Error al validar, pero mantenemos sesión local
                // El usuario podrá seguir usando la app offline
            }
        }
    }

    fun logout() {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = repository.logout()

            result.onSuccess {
                _currentUser.value = null
                _authState.value = AuthState.Logout
            }.onFailure { exception ->
                // Aunque falle, forzamos el logout local
                _currentUser.value = null
                _authState.value = AuthState.Logout
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun updateUserActivity() {
        repository.updateActivity()
    }

    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
}


























