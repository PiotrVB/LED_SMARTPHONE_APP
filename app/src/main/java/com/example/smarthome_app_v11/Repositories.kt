package com.example.smarthome_app_v11

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class LedCommandDto(
    @SerializedName("red") val red: Int,
    @SerializedName("green") val green: Int,
    @SerializedName("blue") val blue: Int,
    @SerializedName("brightness") val brightness: Int
)

interface Esp32ApiService {
    @POST("api/led")
    suspend fun sendLedCommand(@Body command: LedCommandDto): Response<Unit>
}

class DeviceRepository(private val deviceDao: DeviceDao) {
    val devices: Flow<List<DeviceEntity>> = deviceDao.getAllDevices()

    suspend fun getDevice(id: Int) = deviceDao.getDeviceById(id)
    suspend fun addDevice(name: String, ip: String, room: String) {
        deviceDao.insertDevice(DeviceEntity(name = name, ipAddress = ip, roomName = room))
    }
    suspend fun deleteDevice(device: DeviceEntity) = deviceDao.deleteDevice(device)
}

class LedRepository {
    suspend fun sendCommand(ipAddress: String, command: LedCommandDto): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("http://$ipAddress/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(Esp32ApiService::class.java)
                val response = api.sendLedCommand(command)

                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Błąd HTTP ${response.code()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}