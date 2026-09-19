package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.datastore.SessionManager
import com.example.core.navigation.Screen
import com.example.core.ui.ViewModelFactory
import com.example.feature.auth.*
import com.example.feature.main.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sessionManager = SessionManager.getInstance(this)
        val factory = ViewModelFactory.createFactory(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tokenState by sessionManager.authToken.collectAsState(initial = null)
                    
                    if (tokenState == null) {
                        // Secure checking session splash loader
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val isLoggedIn = tokenState!!.isNotEmpty()
                        
                        if (isLoggedIn) {
                            MainScreen(
                                onLogout = {
                                    // SessionManager.clearSession is handled inside ViewModel
                                }
                            )
                        } else {
                            val navController = rememberNavController()
                            val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

                            NavHost(
                                navController = navController,
                                startDestination = Screen.Welcome.route
                            ) {
                                composable(Screen.Welcome.route) {
                                    WelcomeScreen(
                                        onLoginClick = { navController.navigate(Screen.Login.route) },
                                        onRegisterClick = { navController.navigate(Screen.Register.route) }
                                    )
                                }
                                composable(Screen.Login.route) {
                                    LoginScreen(
                                        viewModel = authViewModel,
                                        onLoginSuccess = {
                                            // Managed via tokenState state-flow automatically
                                        },
                                        onRegisterNavigate = { navController.navigate(Screen.Register.route) },
                                        onForgotNavigate = { navController.navigate(Screen.ForgotPassword.route) }
                                    )
                                }
                                composable(Screen.Register.route) {
                                    RegisterScreen(
                                        viewModel = authViewModel,
                                        onRegisterSuccess = {
                                            // Managed via tokenState state-flow automatically
                                        },
                                        onLoginNavigate = { navController.navigate(Screen.Login.route) }
                                    )
                                }
                                composable(Screen.ForgotPassword.route) {
                                    ForgotPasswordScreen(
                                        viewModel = authViewModel,
                                        onBackToLogin = { navController.navigate(Screen.Login.route) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
