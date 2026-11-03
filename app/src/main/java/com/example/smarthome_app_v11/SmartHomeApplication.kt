package com.example.smarthome_app_v11

import android.app.Application
import androidx.room.Room

class SmartHomeApplication : Application() {
    private val repositories = mutableMapOf<String, DeviceRepository>()
    @Synchronized
    fun devicesFor(account: Account): DeviceRepository = repositories.getOrPut(account.id) {
        val database = Room.databaseBuilder(this, AppDatabase::class.java, account.databaseName)
            .addMigrations(AppDatabase.MIGRATION_1_2).build()
        DeviceRepository(database.deviceDao())
    }
    val led by lazy { LedRepository() }
    val auth by lazy { AuthRepository(this) }
}
