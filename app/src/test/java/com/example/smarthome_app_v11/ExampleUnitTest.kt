package com.example.smarthome_app_v11

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ValidationTest {
    @Test fun ipv4AcceptsValidAddresses() {
        listOf("0.0.0.0", "255.255.255.255", "192.168.1.100").forEach { assertTrue(it, Validation.ipv4(it)) }
    }
    @Test fun ipv4RejectsAmbiguousAndInvalidAddresses() {
        listOf("256.0.0.1", "192.168.1", "01.2.3.4", "1.2.3.-1", "1.2.3.4:80", "localhost", "1.2.3.4/", "1.2.3. 4").forEach {
            assertFalse(it, Validation.ipv4(it))
        }
    }
    @Test fun emailRequiresDomainAndAddress() {
        assertTrue(Validation.email(" jan.kowalski@example.com "))
        listOf("abc", "@example.com", "jan@", "jan @example.com").forEach { assertFalse(Validation.email(it)) }
    }
    @Test fun deviceRequiresNameAndValidAddress() {
        assertNotNull(Validation.device("  ", "192.168.1.1"))
        assertNotNull(Validation.device("Lampa", "300.1.1.1"))
        assertNotNull(Validation.device("x".repeat(81), "192.168.1.1"))
        assertNull(Validation.device(" Lampa ", " 192.168.1.1 "))
    }
}

class DeviceRepositoryTest {
    private val dao = MemoryDao()
    private val repository = DeviceRepository(dao)
    @Test fun createEditAndDeletePreserveIdentityAndColor() = runTest {
        repository.save(null, " Biurko ", "192.168.1.50", "")
        val initial = dao.rows.value.single()
        assertEquals("Biurko", initial.name)
        assertEquals("Główne", initial.roomName)
        repository.saveColor(initial.id, LedCommandDto(12, 34, 56, 70))
        repository.save(initial.id, "Salon", "192.168.1.51", "Pokój")
        val updated = repository.getDevice(initial.id)!!
        assertEquals(initial.id, updated.id)
        assertEquals("Salon", updated.name)
        assertEquals("192.168.1.51", updated.ipAddress)
        assertEquals(12, updated.red)
        assertEquals(70, updated.brightness)
        repository.deleteDevice(updated)
        assertTrue(dao.rows.value.isEmpty())
    }
    @Test fun rejectsInvalidDataBeforeWriting() = runTest {
        try { repository.save(null, "Lampa", "999.1.1.1", ""); fail("Expected rejection") }
        catch (_: IllegalArgumentException) { }
        assertTrue(dao.rows.value.isEmpty())
    }
    @Test fun editingMissingDeviceReportsFailure() = runTest {
        try { repository.save(99, "Lampa", "192.168.1.1", ""); fail("Expected missing device") }
        catch (_: IllegalStateException) { }
        assertTrue(dao.rows.value.isEmpty())
    }
    @Test fun rejectsOutOfRangeLedValues() = runTest {
        repository.save(null, "Lampa", "192.168.1.1", "")
        try { repository.saveColor(1, LedCommandDto(256, 0, 0, 100)); fail("Expected rejection") }
        catch (_: IllegalArgumentException) { }
        assertEquals(255, repository.getDevice(1)!!.red)
    }
}

private class MemoryDao : DeviceDao {
    val rows = MutableStateFlow<List<DeviceEntity>>(emptyList())
    private var nextId = 1
    override fun getAllDevices() = rows
    override suspend fun getDeviceById(id: Int) = rows.value.find { it.id == id }
    override suspend fun insertDevice(device: DeviceEntity) { rows.value += device.copy(id = nextId++) }
    override suspend fun updateDetails(id: Int, name: String, ip: String, room: String): Int {
        if (getDeviceById(id) == null) return 0
        rows.value = rows.value.map { if (it.id == id) it.copy(name = name, ipAddress = ip, roomName = room) else it }
        return 1
    }
    override suspend fun updateColor(id: Int, red: Int, green: Int, blue: Int, brightness: Int): Int {
        if (getDeviceById(id) == null) return 0
        rows.value = rows.value.map { if (it.id == id) it.copy(red = red, green = green, blue = blue, brightness = brightness) else it }
        return 1
    }
    override suspend fun deleteDevice(device: DeviceEntity): Int {
        val found = rows.value.any { it.id == device.id }
        rows.value = rows.value.filterNot { it.id == device.id }
        return if (found) 1 else 0
    }
}
