package com.example.smarthome_app_v11

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceScreen(viewModel: DeviceEditorViewModel, editing: Boolean, onBack: () -> Unit) {
    val name by viewModel.name.collectAsState()
    val ip by viewModel.ip.collectAsState()
    val room by viewModel.room.collectAsState()
    val error by viewModel.error.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val ready by viewModel.ready.collectAsState()
    val completed by viewModel.completed.collectAsState()
    LaunchedEffect(completed) { if (completed) onBack() }
    Scaffold(topBar = { TopAppBar(title = { Text(if (editing) "Edytuj ESP32" else "Dodaj ESP32") },
        navigationIcon = { TextButton(onClick = onBack, enabled = !busy) { Text("Wróć") } }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(24.dp)) {
                OutlinedTextField(name, { viewModel.change("name", it) }, label = { Text("Nazwa") }, enabled = ready && !busy, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(ip, { viewModel.change("ip", it) }, label = { Text("Adres IPv4") }, enabled = ready && !busy, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(room, { viewModel.change("room", it) }, label = { Text("Pomieszczenie") }, enabled = ready && !busy, singleLine = true, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 12.dp)) }
                Spacer(Modifier.height(24.dp))
                Button(onClick = viewModel::save, enabled = ready && !busy && !completed, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Zapisywanie…" else "Zapisz urządzenie") }
            }
        }
    }
}
