package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LauncherRepository(
    private val context: Context,
    private val dao: LauncherDao
) {
    val settingsFlow: Flow<ParentalSettings?> = dao.getSettingsFlow()
    val restrictionsFlow: Flow<List<AppRestriction>> = dao.getAllAppRestrictionsFlow()
    val recentLogsFlow: Flow<List<LaunchLog>> = dao.getRecentLogsFlow()

    suspend fun getSettings(): ParentalSettings {
        val existing = dao.getSettings()
        if (existing == null) {
            val defaultSettings = ParentalSettings(
                pin = "0000",
                lastActiveDate = getCurrentDateString()
            )
            dao.saveSettings(defaultSettings)
            return defaultSettings
        }
        return existing
    }

    suspend fun updateSettings(settings: ParentalSettings) {
        dao.saveSettings(settings)
    }

    suspend fun setPin(newPin: String) {
        val settings = getSettings()
        dao.saveSettings(settings.copy(pin = newPin))
    }

    suspend fun setRecoveryEmail(email: String) {
        val settings = getSettings()
        dao.saveSettings(settings.copy(recoveryEmail = email))
    }

    suspend fun setThemeMode(themeMode: String) {
        val settings = getSettings()
        dao.saveSettings(settings.copy(themeMode = themeMode))
    }

    suspend fun updateAppBlockedStatus(packageName: String, isBlocked: Boolean) {
        dao.updateAppBlockedStatus(packageName, isBlocked)
    }

    suspend fun updateAppCategory(packageName: String, category: String) {
        dao.updateAppCategory(packageName, category)
    }

    suspend fun setCategoryBlocked(category: String, isBlocked: Boolean) {
        dao.updateCategoryBlockedStatus(category, isBlocked)
    }

    // Temporary active recovery OTP memory with 10-minute expiry
    private var activeRecoveryOtp: String? = null
    private var activeRecoveryOtpExpiry: Long = 0L

    fun generateRecoveryOtp(): String {
        val otp = (100000..999999).random().toString()
        activeRecoveryOtp = otp
        activeRecoveryOtpExpiry = System.currentTimeMillis() + (10 * 60 * 1000L)
        return otp
    }

    fun getActiveOtp(): String? = activeRecoveryOtp

    suspend fun verifyRecoveryOtpAndResetPin(otp: String, newPin: String): Boolean {
        val now = System.currentTimeMillis()
        if (activeRecoveryOtp != null && activeRecoveryOtp == otp.trim() && now <= activeRecoveryOtpExpiry) {
            setPin(newPin)
            activeRecoveryOtp = null
            activeRecoveryOtpExpiry = 0L
            return true
        }
        return false
    }

    suspend fun logAppLaunch(packageName: String, appName: String) {
        dao.incrementLaunchCount(packageName)
        dao.insertLaunchLog(LaunchLog(packageName = packageName, appName = appName))
    }

    suspend fun clearLogs() {
        dao.clearLaunchLogs()
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun autoDetectCategory(packageName: String, appName: String): String {
        val lower = "$packageName $appName".lowercase()
        return when {
            listOf("game", "play", "minecraft", "roblox", "arcade", "puzzle", "racing", "chess", "mario", "lego", "sonic").any { lower.contains(it) } -> "Games"
            listOf("edu", "learn", "kids", "school", "khan", "duolingo", "math", "book", "read", "study", "science", "abc", "quiz").any { lower.contains(it) } -> "Educational"
            listOf("stream", "video", "tv", "movie", "netflix", "youtube", "disney", "prime", "hulu", "hbo", "plex", "media", "spotify", "music", "audio", "twitch", "tubi").any { lower.contains(it) } -> "Streaming"
            listOf("browser", "chrome", "firefox", "calculator", "clock", "calendar", "desk", "file", "gallery", "settings", "tool", "camera").any { lower.contains(it) } -> "Productivity"
            else -> "Other"
        }
    }

    // This checks and calculates screentime consumed today.
    // Call this periodically to update the active tracking state.
    suspend fun updateScreentimeTracking(isActive: Boolean): ParentalSettings {
        val settings = getSettings()
        val now = System.currentTimeMillis()
        val todayStr = getCurrentDateString()

        val isNewDay = settings.lastActiveDate != todayStr
        val updatedUsage = if (isNewDay) {
            0L
        } else {
            val diff = now - settings.lastActiveTimestamp
            // Accumulate if active and was updated in the last 2 minutes (prevents jump if device was asleep)
            if (isActive && diff > 0 && diff < 120_000) {
                settings.usageTodayMs + diff
            } else {
                settings.usageTodayMs
            }
        }

        val updated = settings.copy(
            usageTodayMs = updatedUsage,
            lastActiveTimestamp = now,
            lastActiveDate = todayStr
        )
        dao.saveSettings(updated)
        return updated
    }

    // Extends screentime for today by adding a bonus
    suspend fun addBonusScreentime(minutes: Int) {
        val settings = getSettings()
        // Deduct from usageTodayMs to "give" them extra time
        val bonusMs = minutes * 60 * 1000L
        val newUsage = (settings.usageTodayMs - bonusMs).coerceAtLeast(0L)
        dao.saveSettings(settings.copy(usageTodayMs = newUsage))
    }

    // Sync physical applications with DB
    suspend fun syncInstalledApps() {
        val pm = context.packageManager
        val selfPkg = context.packageName

        // Query Leanback (TV) and standard launchers
        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val tvApps = pm.queryIntentActivities(tvIntent, 0)

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val mainApps = pm.queryIntentActivities(mainIntent, 0)

        // Merge packages uniquely
        val installedMap = mutableMapOf<String, String>()
        for (info in tvApps + mainApps) {
            val pkg = info.activityInfo.packageName
            if (pkg != selfPkg) {
                val label = info.loadLabel(pm).toString()
                installedMap[pkg] = label
            }
        }

        // Get DB items to preserve settings or insert new ones
        val dbApps = dao.getAllAppRestrictions()
        val dbPkgSet = dbApps.map { it.packageName }.toSet()

        val newRestrictions = mutableListOf<AppRestriction>()
        for ((pkg, label) in installedMap) {
            if (!dbPkgSet.contains(pkg)) {
                val detectedCat = autoDetectCategory(pkg, label)
                newRestrictions.add(
                    AppRestriction(
                        packageName = pkg,
                        appName = label,
                        isBlocked = false,
                        category = detectedCat
                    )
                )
            }
        }

        if (newRestrictions.isNotEmpty()) {
            dao.insertAppRestrictions(newRestrictions)
        }
    }

    // Helper to get app info and drawables
    fun getInstalledAppDetails(): List<AppDetail> {
        val pm = context.packageManager
        val selfPkg = context.packageName

        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val tvApps = pm.queryIntentActivities(tvIntent, 0)

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val mainApps = pm.queryIntentActivities(mainIntent, 0)

        val appDetailsMap = mutableMapOf<String, AppDetail>()
        for (info in tvApps + mainApps) {
            val pkg = info.activityInfo.packageName
            if (pkg != selfPkg) {
                val label = info.loadLabel(pm).toString()
                val icon = info.loadIcon(pm)
                appDetailsMap[pkg] = AppDetail(
                    packageName = pkg,
                    appName = label,
                    icon = icon,
                    activityName = info.activityInfo.name
                )
            }
        }
        return appDetailsMap.values.sortedBy { it.appName.lowercase() }
    }
}

data class AppDetail(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val activityName: String
)
