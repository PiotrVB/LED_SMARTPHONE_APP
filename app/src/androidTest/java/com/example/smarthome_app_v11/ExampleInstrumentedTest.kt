package com.example.smarthome_app_v11

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
    }
    @After fun close() { db.close() }
    @Test fun invalidAddressDisplaysValidationError() {
        val vm = DeviceEditorViewModel(DeviceRepository(db.deviceDao()), SavedStateHandle())
        compose.setContent { MaterialTheme { AddDeviceScreen(vm, false) {} } }
        compose.onNodeWithText("Nazwa").performTextInput("Biurko")
        compose.onNodeWithText("Adres IPv4").performTextInput("999.1.1.1")
        compose.onNodeWithText("Zapisz urządzenie").performScrollTo().performClick()
        compose.onNodeWithText("Podaj poprawny adres IPv4, np. 192.168.1.100").assertIsDisplayed()
        runBlocking { assertNull(db.deviceDao().getDeviceById(1)) }
    }
    @Test fun validFormPersistsDevice() {
        val vm = DeviceEditorViewModel(DeviceRepository(db.deviceDao()), SavedStateHandle())
        compose.setContent { MaterialTheme { AddDeviceScreen(vm, false) {} } }
        compose.onNodeWithText("Nazwa").performTextInput("Biurko")
        compose.onNodeWithText("Adres IPv4").performTextInput("192.168.1.50")
        compose.onNodeWithText("Pomieszczenie").performTextInput("Salon")
        compose.onNodeWithText("Zapisz urządzenie").performScrollTo().performClick()
        compose.waitUntil(5000) { vm.completed.value }
        runBlocking { assertEquals("Salon", db.deviceDao().getDeviceById(1)!!.roomName) }
    }
}

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun migrationPreservesDevicesAndSupportsPersistentCrud() = runBlocking {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        try {
            context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use {
                it.execSQL("CREATE TABLE devices (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, ipAddress TEXT NOT NULL, roomName TEXT NOT NULL)")
                it.execSQL("INSERT INTO devices (name, ipAddress, roomName) VALUES ('Biurko', '192.168.1.50', 'Salon')")
                it.version = 1
            }
            var db = Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(AppDatabase.MIGRATION_1_2).build()
            val repo = DeviceRepository(db.deviceDao())
            assertEquals("Biurko", repo.getDevice(1)!!.name)
            assertEquals(255, repo.getDevice(1)!!.red)
            repo.saveColor(1, LedCommandDto(10, 20, 30, 40))
            repo.save(1, "Lampa", "192.168.1.51", "Gabinet")
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(AppDatabase.MIGRATION_1_2).build()
            val restored = db.deviceDao().getDeviceById(1)!!
            assertEquals("Lampa", restored.name)
            assertEquals(40, restored.brightness)
            assertEquals(10, restored.red)
            assertEquals(1, db.deviceDao().deleteDevice(restored))
            assertNull(db.deviceDao().getDeviceById(1))
            db.close()
        } finally { context.deleteDatabase(name) }
    }
}
