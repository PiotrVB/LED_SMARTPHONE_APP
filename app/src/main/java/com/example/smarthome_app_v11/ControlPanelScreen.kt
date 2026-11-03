package com.example.smarthome_app_v11

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPanelScreen(viewModel: ControlViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text(state.device?.name ?: "Sterowanie LED") },
        navigationIcon = { TextButton(onClick = onBack) { Text("Wróć") } }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(80.dp).background(Color(state.red.toInt(), state.green.toInt(), state.blue.toInt()), CircleShape)
                    .semantics { contentDescription = "Podgląd koloru: czerwony ${state.red.toInt()}, zielony ${state.green.toInt()}, niebieski ${state.blue.toInt()}" })
                Spacer(Modifier.height(16.dp))
                ColorSlider("Czerwony", state.red, 255f, state.ready && !state.isLoading) { viewModel.updateColor(it, state.green, state.blue) }
                ColorSlider("Zielony", state.green, 255f, state.ready && !state.isLoading) { viewModel.updateColor(state.red, it, state.blue) }
                ColorSlider("Niebieski", state.blue, 255f, state.ready && !state.isLoading) { viewModel.updateColor(state.red, state.green, it) }
                ColorSlider("Jasność (%)", state.brightness, 100f, state.ready && !state.isLoading, viewModel::updateBrightness)
                state.message?.let { Text(it, modifier = Modifier.padding(vertical = 12.dp)) }
                if (!state.ready) TextButton(onClick = viewModel::loadDevice) { Text("Ponów wczytanie") }
                Button(onClick = viewModel::sendCommand, enabled = state.ready && !state.isLoading, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.isLoading) "Wysyłanie…" else "Zapisz i wyślij do ESP32")
                }
            }
        }
    }
}

@Composable
private fun ColorSlider(label: String, value: Float, max: Float, enabled: Boolean, onChange: (Float) -> Unit) {
    Text("$label: ${value.toInt()}")
    Slider(value = value, onValueChange = onChange, valueRange = 0f..max, enabled = enabled,
        modifier = Modifier.semantics { contentDescription = label })
}
