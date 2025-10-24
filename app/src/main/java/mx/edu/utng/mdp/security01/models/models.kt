package mx.edu.utng.mdp.security01.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val email: String,
    val name: String,
    val token: String? = null
) : Parcelable

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)

sealed class AuthState {
    object Idle : AuthState() // Estado inicial, sin acción
    object Loading : AuthState() // Procesando autenticación
    data class Success(val user: User) : AuthState() // Login exitoso
    data class Error(val message: String) : AuthState() // Ocurrió un error
    object Logout : AuthState() // Usuario cerró sesión
}
