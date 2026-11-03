package com.example.smarthome_app_v11

import androidx.compose.runtime.*
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

@Composable
fun AppNavigation(app: SmartHomeApplication, authRepository: AuthRepository = app.auth,
    repositoryFor: (Account) -> DeviceRepository = app::devicesFor) {
    val auth: AuthViewModel = viewModel(factory = viewModelFactory { initializer { AuthViewModel(authRepository) } })
    val account by auth.account.collectAsState()
    key(account?.id) {
        val nav = rememberNavController()
        if (account == null) {
            NavHost(nav, startDestination = "login") {
                composable("login") { AuthScreen(false, auth) { auth.error.value = null; nav.navigate("register") } }
                composable("register") { AuthScreen(true, auth) { auth.error.value = null; nav.popBackStack() } }
            }
        } else {
            val deviceRepository = remember(account!!.id) { repositoryFor(account!!) }
            NavHost(nav, startDestination = "dashboard") {
                composable("dashboard") {
                    val vm: DeviceViewModel = viewModel(factory = viewModelFactory { initializer { DeviceViewModel(deviceRepository) } })
                    DashboardScreen(vm, { nav.navigate("control/$it") }, { nav.navigate("add") },
                        { nav.navigate("delete") }, { nav.navigate("edit/$it") }, auth::logout, account!!.email)
                }
                composable("delete") {
                    val vm: DeviceViewModel = viewModel(factory = viewModelFactory { initializer { DeviceViewModel(deviceRepository) } })
                    DeleteDeviceScreen(vm) { nav.popBackStack() }
                }
                composable("add") {
                    val vm: DeviceEditorViewModel = viewModel(factory = viewModelFactory { initializer { DeviceEditorViewModel(deviceRepository, createSavedStateHandle()) } })
                    AddDeviceScreen(vm, false) { nav.popBackStack() }
                }
                composable("edit/{deviceId}", arguments = listOf(navArgument("deviceId") { type = NavType.IntType })) {
                    val vm: DeviceEditorViewModel = viewModel(factory = viewModelFactory { initializer { DeviceEditorViewModel(deviceRepository, createSavedStateHandle()) } })
                    AddDeviceScreen(vm, true) { nav.popBackStack() }
                }
                composable("control/{deviceId}", arguments = listOf(navArgument("deviceId") { type = NavType.IntType })) {
                    val vm: ControlViewModel = viewModel(factory = viewModelFactory { initializer { ControlViewModel(deviceRepository, app.led, createSavedStateHandle()) } })
                    ControlPanelScreen(vm) { nav.popBackStack() }
                }
            }
        }
    }
}





