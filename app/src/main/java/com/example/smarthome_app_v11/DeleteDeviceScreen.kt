package com.example.smarthome_app_v11

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDeviceScreen(viewModel: DeviceViewModel, onBack: () -> Unit) {
    val devices by viewModel.devices.collectAsState()
    val error by viewModel.error.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var pending by rememberSaveable { mutableStateOf<Int?>(null) }
    val selected = devices.find { it.id == pending }
    Scaffold(topBar = { TopAppBar(title = { Text("Usuń ESP32") }, navigationIcon = { TextButton(onClick = onBack) { Text("Wróć") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (loading) item { CircularProgressIndicator() }
            else if (devices.isEmpty()) item { Text("Brak urządzeń do usunięcia") }
            items(devices, key = { it.id }) { device ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(device.name, style = MaterialTheme.typography.titleMedium)
                        Text(device.ipAddress)
                        TextButton(onClick = { pending = device.id }) { Text("Usuń ${device.name}") }
                    }
                }
            }
        }
    }
    if (selected != null) AlertDialog(onDismissRequest = { pending = null }, title = { Text("Usunąć urządzenie?") },
        text = { Text("Urządzenie ${selected.name} i zapisane ustawienia LED zostaną usunięte.") },
        confirmButton = { TextButton(onClick = { viewModel.deleteDevice(selected); pending = null }) { Text("Usuń") } },
        dismissButton = { TextButton(onClick = { pending = null }) { Text("Anuluj") } })
}

