package com.example.smarthome_app_v11

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceViewModel(private val repository: DeviceRepository) : ViewModel() {
    val devices = repository.devices

    var nameInput = MutableStateFlow("")
    var ipInput = MutableStateFlow("")
    var roomInput = MutableStateFlow("")
    var errorMsg = MutableStateFlow<String?>(null)

    private val ipRegex = Regex("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.){3}(25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)$")

    fun addDevice(onSuccess: () -> Unit) {
        val name = nameInput.value.trim()
        val ip = ipInput.value.trim()
        val room = roomInput.value.trim()

        if (name.isEmpty()) {
            errorMsg.value = "Podaj nazwę urządzenia"
            return
        }
        if (!ipRegex.matches(ip)) {
            errorMsg.value = "Niepoprawny adres IP (np. 192.168.1.100)"
            return
        }

        viewModelScope.launch {
            repository.addDevice(name, ip, room.ifEmpty { "Główne" })
            nameInput.value = ""
            ipInput.value = ""
            roomInput.value = ""
            errorMsg.value = null
            onSuccess()
        }
    }

    fun deleteDevice(device: DeviceEntity) {
        viewModelScope.launch {
            repository.deleteDevice(device)
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
    val message: String? = null
)

class ControlViewModel(
    private val deviceRepo: DeviceRepository,
    private val ledRepo: LedRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ControlState())
    val state: StateFlow<ControlState> = _state

    fun loadDevice(id: Int) {
        viewModelScope.launch {
            val dev = deviceRepo.getDevice(id)
            _state.value = _state.value.copy(device = dev)
        }
    }

    fun updateColor(r: Float, g: Float, b: Float) {
        _state.value = _state.value.copy(red = r, green = g, blue = b)
    }

    fun updateBrightness(b: Float) {
        _state.value = _state.value.copy(brightness = b)
    }

    fun sendCommand() {
        val st = _state.value
        val ip = st.device?.ipAddress ?: return
        _state.value = st.copy(isLoading = true, message = null)

        viewModelScope.launch {
            val res = ledRepo.sendCommand(
                ip, LedCommandDto(st.red.toInt(), st.green.toInt(), st.blue.toInt(), st.brightness.toInt())
            )
            res.onSuccess {
                _state.value = _state.value.copy(isLoading = false, message = "Komenda wysłana!")
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, message = "Błąd połączenia z $ip")
            }
        }
    }
}