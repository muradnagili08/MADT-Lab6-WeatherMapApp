package com.example.weathermap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weathermap.di.ViewModelFactory
import com.example.weathermap.presentation.auth.AuthViewModel
import com.example.weathermap.presentation.auth.LoginScreen
import com.example.weathermap.presentation.auth.RegisterScreen
import com.example.weathermap.presentation.map.MapScreen
import com.example.weathermap.presentation.map.MapViewModel

@Composable
fun NavGraph(
    viewModelFactory: ViewModelFactory,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val authState by authViewModel.uiState.collectAsState()

    val startDestination = if (authState.isAuthenticated) {
        Screen.Map.route
    } else {
        Screen.Login.route
    }

    // Reactive authentication guard: automatically redirect to Login if session expires or is terminated
    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == Screen.Map.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Map.route) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Map Screen (Protected Screen)
        composable(Screen.Map.route) {
            if (!authState.isAuthenticated) {
                // Defensive guard: redirect immediately if unauthenticated
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Map.route) { inclusive = true }
                    }
                }
            } else {
                val mapViewModel: MapViewModel = viewModel(factory = viewModelFactory)

                MapScreen(
                    viewModel = mapViewModel,
                    onLogout = {
                        authViewModel.signOut(
                            onLoggedOut = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Map.route) { inclusive = true }
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
