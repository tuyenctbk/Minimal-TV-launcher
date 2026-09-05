package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "parental_settings")
data class ParentalSettings(
    @PrimaryKey val id: Int = 0,
    val pin: String = "0000",
    val recoveryEmail: String = "parent@home.com",
    val dailyLimitMinutes: Int = 60, // 0 for unlimited
    val bedtimeStartHour: Int = 21,  // 9 PM
    val bedtimeStartMinute: Int = 0,
    val bedtimeEndHour: Int = 6,     // 6 AM
    val bedtimeEndMinute: Int = 0,
    val usageTodayMs: Long = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val lastActiveDate: String = "", // format: YYYY-MM-DD
    val themeMode: String = "DARK"   // "DARK" or "LIGHT"
)

@Entity(tableName = "app_restrictions")
data class AppRestriction(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false,
    val launchCount: Int = 0,
    val category: String = "Other" // "Games", "Educational", "Streaming", "Productivity", "Other"
)

@Entity(tableName = "launch_logs")
data class LaunchLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface LauncherDao {
    @Query("SELECT * FROM parental_settings WHERE id = 0")
    fun getSettingsFlow(): Flow<ParentalSettings?>

    @Query("SELECT * FROM parental_settings WHERE id = 0")
    suspend fun getSettings(): ParentalSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ParentalSettings)

    // App Restrictions
    @Query("SELECT * FROM app_restrictions")
    fun getAllAppRestrictionsFlow(): Flow<List<AppRestriction>>

    @Query("SELECT * FROM app_restrictions")
    suspend fun getAllAppRestrictions(): List<AppRestriction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppRestriction(restriction: AppRestriction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppRestrictions(restrictions: List<AppRestriction>)

    @Query("UPDATE app_restrictions SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun updateAppBlockedStatus(packageName: String, isBlocked: Boolean)

    @Query("UPDATE app_restrictions SET category = :category WHERE packageName = :packageName")
    suspend fun updateAppCategory(packageName: String, category: String)

    @Query("UPDATE app_restrictions SET isBlocked = :isBlocked WHERE category = :category")
    suspend fun updateCategoryBlockedStatus(category: String, isBlocked: Boolean)

    @Query("UPDATE app_restrictions SET launchCount = launchCount + 1 WHERE packageName = :packageName")
    suspend fun incrementLaunchCount(packageName: String)

    // Launch Logs
    @Query("SELECT * FROM launch_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogsFlow(): Flow<List<LaunchLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaunchLog(log: LaunchLog)

    @Query("DELETE FROM launch_logs")
    suspend fun clearLaunchLogs()
}

@Database(entities = [ParentalSettings::class, AppRestriction::class, LaunchLog::class], version = 2, exportSchema = false)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao
}
