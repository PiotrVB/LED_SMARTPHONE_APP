package com.example.smarthome_app_v11

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ipAddress: String,
    val roomName: String,
    @ColumnInfo(defaultValue = "255") val red: Int = 255,
    @ColumnInfo(defaultValue = "255") val green: Int = 255,
    @ColumnInfo(defaultValue = "255") val blue: Int = 255,
    @ColumnInfo(defaultValue = "100") val brightness: Int = 100
)

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY name COLLATE NOCASE")
    fun getAllDevices(): Flow<List<DeviceEntity>>
    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getDeviceById(id: Int): DeviceEntity?
    @Insert
    suspend fun insertDevice(device: DeviceEntity)
    @Query("UPDATE devices SET name = :name, ipAddress = :ip, roomName = :room WHERE id = :id")
    suspend fun updateDetails(id: Int, name: String, ip: String, room: String): Int
    @Query("UPDATE devices SET red = :red, green = :green, blue = :blue, brightness = :brightness WHERE id = :id")
    suspend fun updateColor(id: Int, red: Int, green: Int, blue: Int, brightness: Int): Int
    @Delete
    suspend fun deleteDevice(device: DeviceEntity): Int
}

@Database(entities = [DeviceEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE devices ADD COLUMN red INTEGER NOT NULL DEFAULT 255")
                db.execSQL("ALTER TABLE devices ADD COLUMN green INTEGER NOT NULL DEFAULT 255")
                db.execSQL("ALTER TABLE devices ADD COLUMN blue INTEGER NOT NULL DEFAULT 255")
                db.execSQL("ALTER TABLE devices ADD COLUMN brightness INTEGER NOT NULL DEFAULT 100")
            }
        }
    }
}
