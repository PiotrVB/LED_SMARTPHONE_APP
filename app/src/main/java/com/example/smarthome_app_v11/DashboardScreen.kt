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
fun DashboardScreen(viewModel: DeviceViewModel, onControl: (Int) -> Unit, onAdd: () -> Unit,
    onDelete: () -> Unit, onEdit: (Int) -> Unit, onLogout: () -> Unit, accountEmail: String = "") {
    val devices by viewModel.devices.collectAsState()
    val error by viewModel.error.collectAsState()
    val loading by viewModel.loading.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Column { Text("Urządzenia ESP32"); Text(accountEmail, style = MaterialTheme.typography.bodySmall) } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onLogout) { Text("Wyloguj") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAdd) { Text("Dodaj ESP32") }
                    OutlinedButton(onClick = onDelete) { Text("Usuwanie") }
                }
            }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (loading) item { CircularProgressIndicator() }
            else if (devices.isEmpty()) item { Text("Brak urządzeń. Dodaj pierwsze ESP32.") }
            items(devices, key = { it.id }) { device ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(device.name, style = MaterialTheme.typography.titleMedium)
                        Text("IP: ${device.ipAddress}")
                        Text("Pomieszczenie: ${device.roomName}")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = { onControl(device.id) }) { Text("Steruj") }
                            TextButton(onClick = { onEdit(device.id) }) { Text("Edytuj") }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}




