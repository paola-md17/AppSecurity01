package mx.edu.utng.mdp.security01.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import android.util.Log
import kotlinx.coroutines.delay
import mx.edu.utng.mdp.security01.models.LoginRequest
import mx.edu.utng.mdp.security01.models.LoginResponse
import mx.edu.utng.mdp.security01.models.User
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit


interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("auth/validate")
    suspend fun validateToken(@Header("Authorization") token: String): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
}

object RetrofitClient {

    // URL base de la API - CAMBIAR POR LA URL REAL
    private const val BASE_URL = "https://api.ejemplo.com/"

    // Tag para logs - NUNCA mostrar tokens aquí
    private const val TAG = "RetrofitClient"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        // Filtramos información sensible antes de hacer log
        val filteredMessage = filterSensitiveData(message)
        Log.d(TAG, filteredMessage)
    }.apply {
        // CAMBIAR A NONE en producción
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            // Aquí podemos agregar headers comunes a todas las peticiones
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS) // Tiempo máximo para conectar
        .readTimeout(30, TimeUnit.SECONDS)    // Tiempo máximo para leer respuesta
        .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo máximo para enviar datos
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    private fun filterSensitiveData(message: String): String {
        var filtered = message

        // Ocultamos passwords
        if (filtered.contains("password")) {
            filtered = filtered.replace(
                Regex("\"password\"\\s*:\\s*\"[^\"]*\""),
                "\"password\":\"***HIDDEN***\""
            )
        }

        // Ocultamos tokens completos, solo mostramos primeros y últimos 4 caracteres
        if (filtered.contains("Authorization")) {
            filtered = filtered.replace(
                Regex("Bearer [A-Za-z0-9._-]+"),
                "Bearer ****"
            )
        }

        if (filtered.contains("\"token\"")) {
            filtered = filtered.replace(
                Regex("\"token\"\\s*:\\s*\"([^\"]{4})[^\"]*([^\"]{4})\""),
                "\"token\":\"$1****$2\""
            )
        }

        return filtered
    }
}

object MockApiService {

    // Usuarios de prueba
    private val mockUsers = listOf(
        User(
            id = "1",
            email = "alumno@utng.edu.mx",
            name = "Estudiante Demo",
            token = generateToken()
        ),
        User(
            id = "2",
            email = "profesor@utng.edu.mx",
            name = "Profesor Demo",
            token = generateToken()
        ),
        User(
            id = "3",
            email = "admin@utng.edu.mx",
            name = "Administrador Demo",
            token = generateToken()
        )
    )

    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        // Simulamos latencia de red (1-2 segundos)
        delay((1000..2000).random().toLong())

        // Validaciones básicas
        if (loginRequest.email.isBlank() || loginRequest.password.isBlank()) {
            return Response.success(
                LoginResponse(
                    success = false,
                    message = "Email y contraseña son obligatorios",
                    user = null
                )
            )
        }

        val user = mockUsers.find { it.email == loginRequest.email }

        return if (user != null && loginRequest.password == "123456") {
            // Login exitoso
            val userWithNewToken = user.copy(token = generateToken())
            Response.success(
                LoginResponse(
                    success = true,
                    message = "Login exitoso",
                    user = userWithNewToken
                )
            )
        } else {
            // Credenciales incorrectas
            Response.success(
                LoginResponse(
                    success = false,
                    message = "Credenciales incorrectas",
                    user = null
                )
            )
        }
    }
    suspend fun validateToken(token: String): Response<LoginResponse> {
        delay(500)

        // Simulamos que todos los tokens son válidos
        // En producción, el servidor verificaría la firma JWT
        return Response.success(
            LoginResponse(
                success = true,
                message = "Token válido",
                user = null
            )
        )
    }

    /**
     * Simula el logout
     */
    suspend fun logout(token: String): Response<Unit> {
        delay(300)
        return Response.success(Unit)
    }

    private fun generateToken(): String {
        val randomPart = UUID.randomUUID().toString().replace("-", "")
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.$randomPart.mock_signature"
    }
}














