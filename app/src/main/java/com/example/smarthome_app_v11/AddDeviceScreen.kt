package com.example.smarthome_app_v11

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceScreen(
    viewModel: DeviceViewModel,
    onDeviceAdded: () -> Unit
) {
    val name by viewModel.nameInput.collectAsState()
    val ip by viewModel.ipInput.collectAsState()
    val room by viewModel.roomInput.collectAsState()
    val error by viewModel.errorMsg.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Okno 2: Dodaj nowe ESP32") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.nameInput.value = it },
                label = { Text("Nazwa (np. LED Biurko)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = ip,
                onValueChange = { viewModel.ipInput.value = it },
                label = { Text("Adres IP (np. 192.168.1.150)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = room,
                onValueChange = { viewModel.roomInput.value = it },
                label = { Text("Pomieszczenie") },
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.addDevice(onDeviceAdded) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Zapisz moduł ESP32")
            }
        }
    }
}