package com.example.smarthome_app_v11

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.util.Base64
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {
    private fun isolatedContext(prefix: String): Context {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        return object : ContextWrapper(base) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = base.getSharedPreferences(prefix + name, mode)
        }
    }
    @Test fun multipleAccountsAuthenticateIndependentlyAndRejectDuplicateEmail() = runBlocking {
        val prefix = "test_${UUID.randomUUID()}_"
        val context = isolatedContext(prefix)
        try {
            val repository = AuthRepository(context)
            val first = repository.register("Jan@example.com", "Haslo12345")
            val second = repository.register("anna@example.com", "InneHaslo123")
            assertNotEquals(first.id, second.id)
            assertNotEquals(first.databaseName, second.databaseName)
            assertEquals(first, AuthRepository(context).login(" JAN@example.com ", "Haslo12345"))
            assertEquals(second, repository.login("anna@example.com", "InneHaslo123"))
            try { repository.login("jan@example.com", "InneHaslo123"); fail("Other account password accepted") }
            catch (_: IllegalStateException) { }
            try { repository.login("brak@example.com", "Haslo12345"); fail("Unknown account accepted") }
            catch (_: IllegalStateException) { }
            try { repository.register(" JAN@example.com ", "Haslo12345"); fail("Duplicate account accepted") }
            catch (_: IllegalStateException) { }
            val stored = context.getSharedPreferences("account", Context.MODE_PRIVATE).all.toString()
            assertFalse(stored.contains("Haslo12345"))
            assertFalse(stored.contains("jan@example.com"))
        } finally { context.deleteSharedPreferences(prefix + "account") }
    }
    @Test fun legacyAccountKeepsItsIdentityAndOriginalDatabaseAfterRegistrationOfAnotherAccount() = runBlocking {
        val prefix = "legacy_${UUID.randomUUID()}_"
        val context = isolatedContext(prefix)
        try {
            val repository = AuthRepository(context)
            repository.register("jan@example.com", "Haslo12345")
            val prefs = context.getSharedPreferences("account", Context.MODE_PRIVATE)
            val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val key = store.getKey("smarthome_account", null) as SecretKey
            val decrypt = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, Base64.decode(prefs.getString("iv", ""), Base64.NO_WRAP)))
            }
            val envelope = JSONObject(String(decrypt.doFinal(Base64.decode(prefs.getString("data", ""), Base64.NO_WRAP)), Charsets.UTF_8))
            val legacy = envelope.getJSONArray("accounts").getJSONObject(0)
            legacy.remove("id"); legacy.remove("databaseName")
            val encrypt = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key) }
            prefs.edit().putString("data", Base64.encodeToString(encrypt.doFinal(legacy.toString().toByteArray(Charsets.UTF_8)), Base64.NO_WRAP))
                .putString("iv", Base64.encodeToString(encrypt.iv, Base64.NO_WRAP)).commit()
            val first = repository.login("jan@example.com", "Haslo12345")
            assertEquals("smarthome_db", first.databaseName)
            repository.register("anna@example.com", "InneHaslo123")
            assertEquals(first, AuthRepository(context).login("jan@example.com", "Haslo12345"))
        } finally { context.deleteSharedPreferences(prefix + "account") }
    }
    @Test fun accountDatabasesKeepDevicesAndLedValuesSeparate() = runBlocking {
        val prefix = "isolation_${UUID.randomUUID()}_"
        val context = isolatedContext(prefix)
        val databases = mutableListOf<AppDatabase>()
        val names = mutableListOf<String>()
        try {
            val auth = AuthRepository(context)
            val first = auth.register("jan@example.com", "Haslo12345")
            val second = auth.register("anna@example.com", "InneHaslo123")
            fun repository(account: Account): DeviceRepository {
                val name = prefix + account.databaseName
                names.add(name)
                val db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
                databases.add(db)
                return DeviceRepository(db.deviceDao())
            }
            val one = repository(first)
            val two = repository(second)
            one.save(null, "Biurko Jana", "192.168.1.50", "Gabinet")
            assertNull(two.getDevice(1))
            two.save(null, "Lampa Anny", "192.168.1.51", "Salon")
            two.saveColor(1, LedCommandDto(1, 2, 3, 40))
            assertEquals("Biurko Jana", one.getDevice(1)!!.name)
            assertEquals(255, one.getDevice(1)!!.red)
            assertEquals("Lampa Anny", two.getDevice(1)!!.name)
            one.deleteDevice(one.getDevice(1)!!)
            assertNotNull(two.getDevice(1))
        } finally {
            databases.forEach { it.close() }
            names.forEach { context.deleteDatabase(it) }
            context.deleteSharedPreferences(prefix + "account")
        }
    }
}
