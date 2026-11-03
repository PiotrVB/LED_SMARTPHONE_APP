package com.example.smarthome_app_v11

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation(
    deviceRepository: DeviceRepository,
    ledRepository: LedRepository
) {
    val navController = rememberNavController()
    val deviceViewModel = DeviceViewModel(deviceRepository)
    val controlViewModel = ControlViewModel(deviceRepository, ledRepository)

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Okno 1
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = deviceViewModel,
                onNavigateToControl = { id -> navController.navigate(Screen.ControlPanel.createRoute(id)) },
                onNavigateToAdd = { navController.navigate(Screen.AddDevice.route) },
                onNavigateToDelete = { navController.navigate(Screen.DeleteDevice.route) }
            )
        }

        // Okno 2
        composable(Screen.AddDevice.route) {
            AddDeviceScreen(
                viewModel = deviceViewModel,
                onDeviceAdded = { navController.popBackStack() }
            )
        }

        // Okno 3
        composable(Screen.DeleteDevice.route) {
            DeleteDeviceScreen(
                viewModel = deviceViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Sterowanie
        composable(
            route = Screen.ControlPanel.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("deviceId") ?: 0
            ControlPanelScreen(deviceId = id, viewModel = controlViewModel)
        }
    }
}