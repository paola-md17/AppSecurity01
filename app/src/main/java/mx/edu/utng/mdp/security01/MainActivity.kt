package mx.edu.utng.mdp.security01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import mx.edu.utng.mdp.security01.ui.screens.LoginScreen
import mx.edu.utng.mdp.security01.ui.theme.Security01Theme
import mx.edu.utng.mdp.security01.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

      private val authViewModel: AuthViewModel by viewModels {
        androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Security01Theme {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                    }
                )
            }
        }
    }
}
