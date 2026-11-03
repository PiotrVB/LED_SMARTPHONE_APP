package com.example.smarthome_app_v11

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class LedCommandDto(val red: Int, val green: Int, val blue: Int, val brightness: Int)
interface Esp32ApiService {
    @POST("api/led")
    suspend fun sendLedCommand(@Body command: LedCommandDto): Response<Unit>
}
class DeviceRepository(private val dao: DeviceDao) {
    val devices = dao.getAllDevices()
    suspend fun getDevice(id: Int) = dao.getDeviceById(id)
    suspend fun save(id: Int?, name: String, ip: String, room: String) {
        require(Validation.device(name, ip) == null)
        require(room.trim().length <= 80) { "Pomieszczenie może mieć najwyżej 80 znaków" }
        if (id == null) dao.insertDevice(DeviceEntity(name = name.trim(), ipAddress = ip.trim(), roomName = room.trim().ifEmpty { "Główne" }))
        else check(dao.updateDetails(id, name.trim(), ip.trim(), room.trim().ifEmpty { "Główne" }) == 1) { "Urządzenie nie istnieje" }
    }
    suspend fun saveColor(id: Int, command: LedCommandDto) {
        require(listOf(command.red, command.green, command.blue).all { it in 0..255 } && command.brightness in 0..100)
        check(dao.updateColor(id, command.red, command.green, command.blue, command.brightness) == 1) { "Urządzenie nie istnieje" }
    }
    suspend fun deleteDevice(device: DeviceEntity) {
        check(dao.deleteDevice(device) == 1) { "Urządzenie nie istnieje" }
    }
}
class LedRepository(private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS)
    .callTimeout(10, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build()) {
    suspend fun sendCommand(ip: String, command: LedCommandDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(Validation.ipv4(ip))
            val api = Retrofit.Builder().baseUrl("http://$ip/")
                .client(client).addConverterFactory(GsonConverterFactory.create())
                .build().create(Esp32ApiService::class.java)
            val response = api.sendLedCommand(command)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException("ESP32 zwróciło HTTP ${response.code()}"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

