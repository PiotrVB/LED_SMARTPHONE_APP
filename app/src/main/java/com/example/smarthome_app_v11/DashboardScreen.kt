package com.example.smarthome_app_v11

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DeviceViewModel,
    onNavigateToControl: (Int) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToDelete: () -> Unit
) {
    val devices by viewModel.devices.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Okno 1: Dostępne ESP32") },
                actions = {
                    IconButton(onClick = onNavigateToDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń ESP32")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj ESP32")
            }
        }
    ) { padding ->
        if (devices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Brak dodanych urządzeń ESP32.\nUżyj przycisku +, aby dodać pierwsze.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices) { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToControl(device.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(device.name, style = MaterialTheme.typography.titleMedium)
                                Text("IP: ${device.ipAddress} | Pokój: ${device.roomName}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Steruj >", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}