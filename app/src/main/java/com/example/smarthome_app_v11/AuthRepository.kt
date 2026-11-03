package com.example.smarthome_app_v11

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import org.json.JSONArray
import org.json.JSONObject

data class Account(val id: String, val email: String, val databaseName: String)

class AuthRepository(context: Context) {
    private val preferences = context.getSharedPreferences("account", Context.MODE_PRIVATE)
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey("smarthome_account", null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder("smarthome_account", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
    }
    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String) = Base64.decode(value, Base64.NO_WRAP)
    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 1_300_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
    private fun readAccounts(): List<JSONObject> {
        val encrypted = preferences.getString("data", null) ?: return emptyList()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, decode(preferences.getString("iv", "")!!)))
        }
        val data = JSONObject(String(cipher.doFinal(decode(encrypted)), Charsets.UTF_8))
        if (data.has("accounts")) {
            val array = data.getJSONArray("accounts")
            return List(array.length()) { array.getJSONObject(it) }
        }
        val email = data.getString("email").trim().lowercase(Locale.ROOT)
        data.put("id", UUID.nameUUIDFromBytes(email.toByteArray(Charsets.UTF_8)).toString())
            .put("email", email).put("databaseName", "smarthome_db")
        return listOf(data)
    }
    private fun saveAccounts(accounts: List<JSONObject>) {
        val data = JSONObject().put("accounts", JSONArray(accounts)).toString()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = encode(cipher.doFinal(data.toByteArray(Charsets.UTF_8)))
        check(preferences.edit().putString("data", encrypted).putString("iv", encode(cipher.iv)).commit()) { "Nie udało się zapisać konta" }
    }
    private fun account(data: JSONObject) = Account(data.getString("id"), data.getString("email"), data.getString("databaseName"))

    suspend fun register(email: String, password: String): Account = withContext(Dispatchers.IO) {
        mutex.withLock {
            require(Validation.email(email)) { "Podaj poprawny email" }
            require(password.length in 8..128) { "Hasło musi mieć od 8 do 128 znaków" }
            val normalized = email.trim().lowercase(Locale.ROOT)
            val accounts = readAccounts()
            check(accounts.none { it.getString("email") == normalized }) { "Konto z tym adresem email już istnieje" }
            val id = UUID.randomUUID().toString()
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val data = JSONObject().put("id", id).put("email", normalized)
                .put("databaseName", if (accounts.isEmpty()) "smarthome_db" else "smarthome_$id")
                .put("salt", encode(salt)).put("hash", encode(hash(password, salt)))
            saveAccounts(accounts + data)
            account(data)
        }
    }
    suspend fun login(email: String, password: String): Account = withContext(Dispatchers.IO) {
        mutex.withLock {
            require(Validation.email(email) && password.length in 8..128) { "Niepoprawny email lub hasło" }
            val accounts = readAccounts()
            val normalized = email.trim().lowercase(Locale.ROOT)
            val data = accounts.find { it.getString("email") == normalized }
            val salt = data?.let { decode(it.getString("salt")) } ?: ByteArray(16)
            val candidate = hash(password, salt)
            check(data != null && MessageDigest.isEqual(candidate, decode(data.getString("hash")))) { "Niepoprawny email lub hasło" }
            saveAccounts(accounts)
            account(data)
        }
    }
    companion object {
        private val mutex = Mutex()
    }
}
