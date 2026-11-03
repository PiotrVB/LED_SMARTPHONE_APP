package com.example.smarthome_app_v11

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")        // Okno 1: Lista ESP32
    object AddDevice : Screen("add_device")      // Okno 2: Dodawanie ESP32
    object DeleteDevice : Screen("delete_device")  // Okno 3: Usuwanie ESP32
    object ControlPanel : Screen("control/{deviceId}") { // Panel sterowania
        fun createRoute(deviceId: Int) = "control/$deviceId"
    }
}