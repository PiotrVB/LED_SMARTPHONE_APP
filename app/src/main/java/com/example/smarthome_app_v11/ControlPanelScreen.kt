package com.example.smarthome_app_v11

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPanelScreen(
    deviceId: Int,
    viewModel: ControlViewModel
) {
    LaunchedEffect(deviceId) {
        viewModel.loadDevice(deviceId)
    }

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.device?.name ?: "Sterowanie LED") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(state.red.toInt(), state.green.toInt(), state.blue.toInt()), CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Czerwony: ${state.red.toInt()}")
            Slider(value = state.red, onValueChange = { viewModel.updateColor(it, state.green, state.blue) }, valueRange = 0f..255f)

            Text("Zielony: ${state.green.toInt()}")
            Slider(value = state.green, onValueChange = { viewModel.updateColor(state.red, it, state.blue) }, valueRange = 0f..255f)

            Text("Niebieski: ${state.blue.toInt()}")
            Slider(value = state.blue, onValueChange = { viewModel.updateColor(state.red, state.green, it) }, valueRange = 0f..255f)

            Text("Jasność: ${state.brightness.toInt()}%")
            Slider(value = state.brightness, onValueChange = { viewModel.updateBrightness(it) }, valueRange = 0f..100f)

            state.message?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.sendCommand() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (state.isLoading) "Wysyłanie..." else "Wyślij komendę do ESP32")
            }
        }
    }
}