package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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
                profileName = context.getString(R.string.default_profile_name),
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

    suspend fun setProfileName(name: String) {
        val settings = getSettings()
        dao.saveSettings(settings.copy(profileName = name))
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

    suspend fun incrementAppLaunchCount() {
        val settings = getSettings()
        dao.saveSettings(settings.copy(appLaunchCount = settings.appLaunchCount + 1))
    }

    suspend fun setHasRatedApp(rated: Boolean) {
        val settings = getSettings()
        dao.saveSettings(settings.copy(hasRatedApp = rated, hasDismissedRatingPrompt = rated))
    }

    suspend fun setHasSharedApp(shared: Boolean) {
        val settings = getSettings()
        dao.saveSettings(settings.copy(hasSharedApp = shared, hasDismissedSharePrompt = shared))
    }

    suspend fun dismissRatingPrompt(permanent: Boolean) {
        val settings = getSettings()
        if (permanent) {
            dao.saveSettings(settings.copy(hasDismissedRatingPrompt = true))
        } else {
            dao.saveSettings(settings.copy(nextPromptLaunchThreshold = settings.appLaunchCount + 10))
        }
    }

    suspend fun dismissSharePrompt(permanent: Boolean) {
        val settings = getSettings()
        if (permanent) {
            dao.saveSettings(settings.copy(hasDismissedSharePrompt = true))
        } else {
            dao.saveSettings(settings.copy(nextPromptLaunchThreshold = settings.appLaunchCount + 10))
        }
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

    suspend fun deleteAppRestriction(packageName: String) {
        dao.deleteAppRestriction(packageName)
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

    suspend fun deleteLog(logId: Int) {
        dao.deleteLaunchLog(logId)
    }

    suspend fun clearLogs() {
        dao.clearLaunchLogs()
        dao.resetAllLaunchCounts()
    }

    suspend fun resetTodayUsage() {
        val now = System.currentTimeMillis()
        val settings = getSettings()
        dao.saveSettings(settings.copy(usageTodayMs = 0L, lastActiveTimestamp = now))
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
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
        val now = System.currentTimeMillis()
        val bonusMs = minutes * 60 * 1000L
        val limitMs = settings.dailyLimitMinutes * 60 * 1000L
        // Clamp current usage to daily limit first so granting bonus time always gives the full bonus duration
        val effectiveUsage = if (limitMs > 0L) settings.usageTodayMs.coerceAtMost(limitMs) else settings.usageTodayMs
        val newUsage = (effectiveUsage - bonusMs).coerceAtLeast(0L)
        dao.saveSettings(settings.copy(usageTodayMs = newUsage, lastActiveTimestamp = now))
    }

    // Sync physical applications with DB on IO dispatcher
    suspend fun syncInstalledApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPkg = context.packageName

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val mainApps = pm.queryIntentActivities(mainIntent, 0)

        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val tvApps = pm.queryIntentActivities(tvIntent, 0)

        // Merge packages uniquely: iterate mainApps first so Leanback TV apps take priority
        val installedMap = mutableMapOf<String, String>()
        for (info in mainApps + tvApps) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg != selfPkg) {
                val label = try {
                    info.loadLabel(pm).toString().ifBlank { pkg }
                } catch (_: Exception) {
                    pkg
                }
                installedMap[pkg] = label
            }
        }

        // Get DB items to preserve settings or insert new ones
        val dbApps = dao.getAllAppRestrictions()
        val dbPkgSet = dbApps.map { it.packageName }.toSet()

        // Clean up uninstalled / deleted apps from database
        val installedPkgs = installedMap.keys
        val orphanedPkgs = dbPkgSet - installedPkgs
        if (orphanedPkgs.isNotEmpty()) {
            dao.deleteAppRestrictions(orphanedPkgs.toList())
        }

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

    // Helper to get app info and drawables on IO dispatcher (Leanback TV activities prioritized)
    suspend fun getInstalledAppDetails(): List<AppDetail> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPkg = context.packageName

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val mainApps = pm.queryIntentActivities(mainIntent, 0)

        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val tvApps = pm.queryIntentActivities(tvIntent, 0)

        val fallbackIcon = pm.defaultActivityIcon
        val appDetailsMap = mutableMapOf<String, AppDetail>()
        // Iterate mainApps first so Leanback TV launcher icons/activities overwrite mobile entries
        for (info in mainApps + tvApps) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg != selfPkg) {
                val label = try {
                    info.loadLabel(pm).toString().ifBlank { pkg }
                } catch (_: Exception) {
                    pkg
                }
                val icon = try {
                    info.loadIcon(pm) ?: fallbackIcon
                } catch (_: Throwable) {
                    fallbackIcon
                }
                appDetailsMap[pkg] = AppDetail(
                    packageName = pkg,
                    appName = label,
                    icon = icon,
                    activityName = info.activityInfo.name
                )
            }
        }
        appDetailsMap.values.sortedBy { it.appName.lowercase(Locale.ROOT) }
    }
}

data class AppDetail(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val activityName: String
)
