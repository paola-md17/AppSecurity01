package mx.edu.utng.mdp.security01.ui.screens

import android.util.Patterns
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import mx.edu.utng.mdp.security01.R
import mx.edu.utng.mdp.security01.models.AuthState
import mx.edu.utng.mdp.security01.ui.components.CustomTextField
import mx.edu.utng.mdp.security01.ui.components.ErrorDialog
import mx.edu.utng.mdp.security01.ui.components.LoadingButton
import mx.edu.utng.mdp.security01.viewmodel.AuthViewModel
import androidx.compose.runtime.livedata.observeAsState
import mx.edu.utng.mdp.security01.models.User

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by viewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onLoginSuccess()
            }

            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
                showErrorDialog = true
                viewModel.resetAuthState()
            }

            else -> Unit
        }
    }

    fun validateFields(): Boolean {
        var isValid = true

        when {
            email.isBlank() -> {
                emailError = "El email es obligatorio"
                isValid = false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailError = "Formato de email inválido"
                isValid = false
            }

            else -> emailError = null
        }

        when {
            password.isBlank() -> {
                passwordError = "La contraseña es obligatoria"
                isValid = false
            }

            password.length < 6 -> {
                passwordError = "Mínimo 6 caracteres"
                isValid = false
            }

            else -> passwordError = null
        }

        return isValid
    }

    fun performLogin() {
        if (validateFields()) {
            viewModel.login(email, password)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
           /* Image(
                painter = painterResource(id = R.drawable.ic_security),
                contentDescription = "Logo de la app",
                modifier = Modifier.size(120.dp)
            )*/

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CustomTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = "Correo electrónico",
                leadingIcon = Icons.Default.Email,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                isError = emailError != null,
                errorMessage = emailError,
                enabled = authState !is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "Contraseña",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                onImeAction = { performLogin() },
                isError = passwordError != null,
                errorMessage = passwordError,
                enabled = authState !is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(32.dp))

            LoadingButton(
                text = "Iniciar Sesión",
                onClick = { performLogin() },
                isLoading = authState is AuthState.Loading,
                enabled = authState !is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { /* TODO: Navegar a recuperar contraseña */ },
                enabled = authState !is AuthState.Loading
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Versión 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showErrorDialog) {
            ErrorDialog(
                title = "Error de autenticación",
                message = errorMessage,
                onDismiss = { showErrorDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit
) {

    val authState by viewModel.authState.observeAsState()
    val currentUser by viewModel.currentUser.observeAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Logout) {
            onLogout()
        }
    }

    DisposableEffect(Unit) {
        viewModel.updateUserActivity()
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi Perfil", style = MaterialTheme.typography.titleLarge)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            currentUser?.let { user ->
                UserInfoCard(user = user)

                Spacer(modifier = Modifier.height(32.dp))

                SecuritySection(viewModel = viewModel)

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { Text("Cerrar Sesión", style = MaterialTheme.typography.headlineSmall) },
                text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        }
                    ) {
                        Text("Cerrar Sesión")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

private fun Error.let(block: Any) {}

@Composable
private fun UserInfoCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Usuario ${user}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "correo@example.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ID: $user",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SecuritySection(viewModel: AuthViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Información de Seguridad",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SecurityItem(
                title = "Sesión Encriptada",
                description = "Tus datos están protegidos con encriptación AES-256"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecurityItem(
                title = "Token de Autenticación",
                description = "Token JWT activo y verificado"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecurityItem(
                title = "Expiración de Sesión",
                description = "Tu sesión expirará después de 24 horas de inactividad"
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.validateToken() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Validar Token en Servidor")
            }
        }
    }
}

@Composable
private fun SecurityItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SplashScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val isLoggedIn = viewModel.isLoggedIn()

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
        delay(2000)

        if (isLoggedIn) {
            viewModel.validateToken()
            delay(500)
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_security),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Security App",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
        }
    }
}
