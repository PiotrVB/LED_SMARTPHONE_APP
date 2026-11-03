package com.example.smarthome_app_v11

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(register: Boolean, viewModel: AuthViewModel, onSwitch: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val error by viewModel.error.collectAsState()
    val busy by viewModel.busy.collectAsState()
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (register) "Utwórz lokalne konto" else "Sterownik ESP32 LED", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), enabled = !busy, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Hasło (8–128 znaków)") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !busy, modifier = Modifier.fillMaxWidth())
            if (register) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(confirm, { confirm = it }, label = { Text("Powtórz hasło") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !busy, modifier = Modifier.fillMaxWidth())
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 12.dp)) }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { viewModel.submit(email, password, register, confirm) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (busy) "Proszę czekać…" else if (register) "Zarejestruj konto" else "Zaloguj się")
            }
            TextButton(onClick = onSwitch, enabled = !busy) { Text(if (register) "Mam konto — zaloguj się" else "Utwórz lokalne konto") }
        }
    }
}


