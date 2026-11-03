package com.example.smarthome_app_v11

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var account = MutableStateFlow<Account?>(null)
        private set
    val busy = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    fun submit(email: String, password: String, register: Boolean, confirm: String) {
        if (busy.value) return
        if (register && password != confirm) { error.value = "Hasła nie są identyczne"; return }
        busy.value = true
        error.value = null
        viewModelScope.launch {
            try {
                account.value = if (register) repository.register(email, password) else repository.login(email, password)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = if (e is IllegalArgumentException || e is IllegalStateException) e.message else "Nie udało się odczytać lub zapisać konta" }
            finally { busy.value = false }
        }
    }
    fun logout() { account.value = null; error.value = null }
}

class DeviceViewModel(private val repository: DeviceRepository) : ViewModel() {
    val error = MutableStateFlow<String?>(null)
    val loading = MutableStateFlow(true)
    val devices = repository.devices.onEach { loading.value = false }
        .catch { error.value = "Nie udało się odczytać bazy. Spróbuj ponownie otworzyć aplikację."; loading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun deleteDevice(device: DeviceEntity) {
        viewModelScope.launch {
            try { repository.deleteDevice(device) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = "Nie udało się usunąć urządzenia" }
        }
    }
}

class DeviceEditorViewModel(private val repository: DeviceRepository, private val saved: SavedStateHandle) : ViewModel() {
    private val id = saved.get<Int>("deviceId")?.takeIf { it > 0 }
    val name = saved.getStateFlow("name", "")
    val ip = saved.getStateFlow("ip", "")
    val room = saved.getStateFlow("room", "")
    val error = MutableStateFlow<String?>(null)
    val busy = MutableStateFlow(false)
    val ready = MutableStateFlow(id == null || saved.get<Boolean>("loaded") == true)
    val completed = MutableStateFlow(false)
    init {
        if (!ready.value) viewModelScope.launch {
            try {
                val device = repository.getDevice(id!!) ?: error("Urządzenie nie istnieje")
                saved["name"] = device.name; saved["ip"] = device.ipAddress; saved["room"] = device.roomName
                saved["loaded"] = true; ready.value = true
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = "Nie udało się wczytać urządzenia. Wróć i spróbuj ponownie." }
        }
    }
    fun change(field: String, value: String) { saved[field] = value; error.value = null }
    fun save() {
        if (busy.value || !ready.value) return
        error.value = Validation.device(name.value, ip.value)
        if (error.value != null) return
        if (room.value.trim().length > 80) { error.value = "Pomieszczenie może mieć najwyżej 80 znaków"; return }
        busy.value = true
        viewModelScope.launch {
            try { repository.save(id, name.value, ip.value, room.value); completed.value = true }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = "Nie udało się zapisać urządzenia" }
            finally { busy.value = false }
        }
    }
}

data class ControlState(
    val device: DeviceEntity? = null,
    val red: Float = 255f,
    val green: Float = 255f,
    val blue: Float = 255f,
    val brightness: Float = 100f,
    val isLoading: Boolean = false,
    val message: String? = null,
    val ready: Boolean = false
)
class ControlViewModel(private val devices: DeviceRepository, private val led: LedRepository,
    private val saved: SavedStateHandle) : ViewModel() {
    val state = MutableStateFlow(ControlState())
    init { loadDevice() }
    fun loadDevice() {
        viewModelScope.launch {
            try {
                val device = devices.getDevice(checkNotNull(saved.get<Int>("deviceId"))) ?: error("Urządzenie nie istnieje")
                state.value = ControlState(device, saved.get<Float>("red") ?: device.red.toFloat(),
                    saved.get<Float>("green") ?: device.green.toFloat(), saved.get<Float>("blue") ?: device.blue.toFloat(),
                    saved.get<Float>("brightness") ?: device.brightness.toFloat(), ready = true)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { state.value = state.value.copy(message = "Nie udało się wczytać urządzenia", ready = false) }
        }
    }
    fun updateColor(r: Float, g: Float, b: Float) {
        if (state.value.isLoading) return
        saved["red"] = r; saved["green"] = g; saved["blue"] = b
        state.value = state.value.copy(red = r, green = g, blue = b)
    }
    fun updateBrightness(b: Float) {
        if (state.value.isLoading) return
        saved["brightness"] = b; state.value = state.value.copy(brightness = b)
    }
    fun sendCommand() {
        val st = state.value
        val device = st.device ?: return
        if (st.isLoading || !st.ready) return
        state.value = st.copy(isLoading = true, message = null)
        viewModelScope.launch {
            try {
                val command = LedCommandDto(st.red.toInt(), st.green.toInt(), st.blue.toInt(), st.brightness.toInt())
                devices.saveColor(device.id, command)
                led.sendCommand(device.ipAddress, command).fold(
                    onSuccess = { state.value = state.value.copy(message = "Ustawienia zapisane, komenda wysłana") },
                    onFailure = { state.value = state.value.copy(message = "Ustawienia zapisane. Błąd połączenia z ESP32. Sprawdź adres IP, połączenie z siecią i zasilanie urządzenia.") })
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { state.value = state.value.copy(message = "Nie udało się zapisać ustawień w bazie. Komenda nie została wysłana.") }
            finally { state.value = state.value.copy(isLoading = false) }
        }
    }
}



