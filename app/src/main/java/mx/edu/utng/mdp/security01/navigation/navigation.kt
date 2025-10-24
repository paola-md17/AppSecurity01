package mx.edu.utng.mdp.security01.navigation

import android.window.SplashScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.edu.utng.mdp.security01.ui.screens.HomeScreen
import mx.edu.utng.mdp.security01.ui.screens.LoginScreen
import mx.edu.utng.mdp.security01.ui.screens.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ============================================
        // PANTALLA DE SPLASH
        // ============================================
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        // Eliminamos Splash del stack para que no se pueda volver
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen (
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        // Eliminamos Login del stack
                        // Esto evita que al presionar "atrás" regrese al login
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        // Limpiamos todo el stack de navegación
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}























