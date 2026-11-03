package com.example.smarthome_app_v11

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val compose = createComposeRule()
    @Test fun registerCreateEditDeleteAndLogout() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as SmartHomeApplication
        val prefix = "navigation_${UUID.randomUUID()}_"
        val context = object : ContextWrapper(app) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = app.getSharedPreferences(prefix + name, mode)
        }
        val db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java).build()
        try {
            compose.setContent { MaterialTheme { AppNavigation(app, AuthRepository(context), { DeviceRepository(db.deviceDao()) }) } }
            compose.onNodeWithText("Utwórz lokalne konto").performClick()
            compose.onNodeWithText("Email").performTextInput("test@example.com")
            compose.onNodeWithText("Hasło (8–128 znaków)").performTextInput("Haslo12345")
            compose.onNodeWithText("Powtórz hasło").performTextInput("Haslo12345")
            compose.onNodeWithText("Zarejestruj konto").performScrollTo().performClick()
            compose.waitUntil(30000) { compose.onAllNodesWithText("Urządzenia ESP32").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Dodaj ESP32").performClick()
            compose.onNodeWithText("Nazwa").performTextInput("Biurko")
            compose.onNodeWithText("Adres IPv4").performTextInput("192.168.1.50")
            compose.onNodeWithText("Zapisz urządzenie").performScrollTo().performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Biurko").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Edytuj").performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Biurko").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Nazwa").performTextReplacement("Salon")
            compose.onNodeWithText("Zapisz urządzenie").performScrollTo().performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Urządzenia ESP32").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Salon").assertIsDisplayed()
            compose.onNodeWithText("Usuwanie").performClick()
            compose.onNodeWithText("Usuń Salon").performClick()
            compose.onNodeWithText("Usuń").performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Brak urządzeń do usunięcia").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Wróć").performClick()
            compose.onNodeWithText("Wyloguj").performClick()
            compose.onNodeWithText("Zaloguj się").assertIsDisplayed()
            compose.onAllNodesWithText("Urządzenia ESP32").assertCountEquals(0)
        } finally {
            db.close()
            app.deleteSharedPreferences(prefix + "account")
        }
    }
    @Test fun switchingAccountsShowsOnlyTheirOwnDevices() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as SmartHomeApplication
        val prefix = "switching_${UUID.randomUUID()}_"
        val context = object : ContextWrapper(app) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = app.getSharedPreferences(prefix + name, mode)
        }
        val databases = mutableMapOf<String, AppDatabase>()
        val repositoryFor: (Account) -> DeviceRepository = { account ->
            DeviceRepository(databases.getOrPut(account.id) { Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java).build() }.deviceDao())
        }
        fun waitForDashboard(email: String) {
            compose.waitUntil(30000) { compose.onAllNodesWithText("Urządzenia ESP32").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(email).assertIsDisplayed()
        }
        fun register(email: String) {
            compose.onNodeWithText("Utwórz lokalne konto").performClick()
            compose.onNodeWithText("Email").performTextInput(email)
            compose.onNodeWithText("Hasło (8–128 znaków)").performTextInput("Haslo12345")
            compose.onNodeWithText("Powtórz hasło").performTextInput("Haslo12345")
            compose.onNodeWithText("Zarejestruj konto").performScrollTo().performClick()
            waitForDashboard(email)
        }
        fun add(name: String) {
            compose.onNodeWithText("Dodaj ESP32").performClick()
            compose.onNodeWithText("Nazwa").performTextInput(name)
            compose.onNodeWithText("Adres IPv4").performTextInput("192.168.1.50")
            compose.onNodeWithText("Zapisz urządzenie").performScrollTo().performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
        }
        try {
            compose.setContent { MaterialTheme { AppNavigation(app, AuthRepository(context), repositoryFor) } }
            register("jan@example.com")
            add("Biurko Jana")
            compose.onNodeWithText("Wyloguj").performClick()
            register("anna@example.com")
            compose.onAllNodesWithText("Biurko Jana").assertCountEquals(0)
            add("Lampa Anny")
            compose.onNodeWithText("Wyloguj").performClick()
            compose.onNodeWithText("Email").performTextInput("jan@example.com")
            compose.onNodeWithText("Hasło (8–128 znaków)").performTextInput("Haslo12345")
            compose.onNodeWithText("Zaloguj się").performScrollTo().performClick()
            waitForDashboard("jan@example.com")
            compose.waitUntil(5000) { compose.onAllNodesWithText("Biurko Jana").fetchSemanticsNodes().isNotEmpty() }
            compose.onAllNodesWithText("Lampa Anny").assertCountEquals(0)
        } finally {
            databases.values.forEach { it.close() }
            app.deleteSharedPreferences(prefix + "account")
        }
    }
}



