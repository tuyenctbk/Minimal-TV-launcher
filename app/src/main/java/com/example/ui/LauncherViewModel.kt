package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRestriction
import com.example.data.LaunchLog
import com.example.data.LauncherRepository
import com.example.data.ParentalSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface LauncherUiState {
    object Loading : LauncherUiState
    data class Success(
        val apps: List<LauncherAppItem>,
        val settings: ParentalSettings,
        val restrictions: List<AppRestriction>,
        val recentLogs: List<LaunchLog>,
        val isLocked: Boolean,
        val lockReason: LockReason,
        val remainingScreentimeMs: Long
    ) : LauncherUiState
}

enum class LockReason {
    NONE,
    SCREENTIME,
    BEDTIME
}

data class LauncherAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val activityName: String,
    val isBlocked: Boolean,
    val category: String = "Other"
)

class LauncherViewModel(
    private val repository: LauncherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LauncherUiState>(LauncherUiState.Loading)
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    // Temporary parent override state (e.g., bypass screen lock for parents' session)
    private val _isParentBypassed = MutableStateFlow(false)
    val isParentBypassed: StateFlow<Boolean> = _isParentBypassed.asStateFlow()

    // Current category filter on main screen
    var selectedCategory by mutableStateOf("All")

    fun selectCategory(cat: String) {
        selectedCategory = cat
    }

    // Theme mode flow (DARK or LIGHT)
    private val _themeMode = MutableStateFlow("DARK")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Screen navigation overlay states (not Room managed to avoid DB roundtrips)
    var showParentalSettings by mutableStateOf(false)
    var showPinPromptForSettings by mutableStateOf(false)
    var showPinRecoveryDialog by mutableStateOf(false)
    var pinPromptSuccessCallback: (() -> Unit)? by mutableStateOf(null)

    private var trackingJob: Job? = null

    init {
        viewModelScope.launch {
            // Initial sync of installed apps to local restrictions database
            repository.syncInstalledApps()

            // Combine data flows reactively
            combine(
                repository.settingsFlow,
                repository.restrictionsFlow,
                repository.recentLogsFlow,
                _isParentBypassed
            ) { settingsOpt, restrictions, logs, bypassed ->
                val settings = settingsOpt ?: repository.getSettings()
                _themeMode.value = settings.themeMode
                val fullApps = repository.getInstalledAppDetails()

                val restrictionMap = restrictions.associateBy { it.packageName }
                val mappedApps = fullApps.map { detail ->
                    val restriction = restrictionMap[detail.packageName]
                    LauncherAppItem(
                        packageName = detail.packageName,
                        appName = detail.appName,
                        icon = detail.icon,
                        activityName = detail.activityName,
                        isBlocked = restriction?.isBlocked ?: false,
                        category = restriction?.category ?: repository.autoDetectCategory(detail.packageName, detail.appName)
                    )
                }

                // Check Bedtime and Screentime constraints
                val bedtimeActive = !bypassed && isBedtimeActive(settings)
                val totalLimitMs = settings.dailyLimitMinutes * 60 * 1000L
                val screentimeExhausted = !bypassed && settings.dailyLimitMinutes > 0 && settings.usageTodayMs >= totalLimitMs

                val (isLocked, lockReason) = when {
                    bedtimeActive -> true to LockReason.BEDTIME
                    screentimeExhausted -> true to LockReason.SCREENTIME
                    else -> false to LockReason.NONE
                }

                val remainingMs = if (settings.dailyLimitMinutes > 0) {
                    (totalLimitMs - settings.usageTodayMs).coerceAtLeast(0L)
                } else {
                    -1L // Unlimited
                }

                LauncherUiState.Success(
                    apps = mappedApps,
                    settings = settings,
                    restrictions = restrictions,
                    recentLogs = logs,
                    isLocked = isLocked,
                    lockReason = lockReason,
                    remainingScreentimeMs = remainingMs
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        // Start active tracking of screen time
        startScreentimeTracking()
    }

    private fun startScreentimeTracking() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            while (isActive) {
                delay(2000) // Update every 2 seconds to keep stats accurate but lightweight
                val state = _uiState.value
                val isBypassed = _isParentBypassed.value
                // Only track usage if we are NOT bypassed by parent override
                if (state is LauncherUiState.Success && !isBypassed) {
                    repository.updateScreentimeTracking(isActive = true)
                } else {
                    repository.updateScreentimeTracking(isActive = false)
                }
            }
        }
    }

    fun verifyPin(enteredPin: String, onCorrect: () -> Unit, onIncorrect: () -> Unit) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            if (enteredPin == settings.pin) {
                onCorrect()
            } else {
                onIncorrect()
            }
        }
    }

    fun setParentPin(newPin: String) {
        viewModelScope.launch {
            repository.setPin(newPin)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            _themeMode.value = mode
            repository.setThemeMode(mode)
        }
    }

    fun toggleThemeMode() {
        val nextMode = if (_themeMode.value == "DARK") "LIGHT" else "DARK"
        setThemeMode(nextMode)
    }

    fun setRecoveryEmail(email: String) {
        viewModelScope.launch {
            repository.setRecoveryEmail(email)
        }
    }

    fun sendRecoveryOtp(onGenerated: (code: String, email: String) -> Unit) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val otp = repository.generateRecoveryOtp()
            onGenerated(otp, settings.recoveryEmail)
        }
    }

    fun verifyRecoveryOtpAndResetPin(
        otp: String,
        newPin: String,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.verifyRecoveryOtpAndResetPin(otp, newPin)
            if (success) {
                onSuccess()
            } else {
                onFailed("Invalid or expired 6-digit recovery code. Please retry.")
            }
        }
    }

    fun updateAppCategory(packageName: String, category: String) {
        viewModelScope.launch {
            repository.updateAppCategory(packageName, category)
        }
    }

    fun setCategoryBlocked(category: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.setCategoryBlocked(category, isBlocked)
        }
    }

    fun setDailyLimit(minutes: Int) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            repository.updateSettings(settings.copy(dailyLimitMinutes = minutes))
        }
    }

    fun setBedtimeSchedule(startHour: Int, endHour: Int) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            repository.updateSettings(settings.copy(
                bedtimeStartHour = startHour,
                bedtimeEndHour = endHour
            ))
        }
    }

    fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.updateAppBlockedStatus(packageName, isBlocked)
        }
    }

    fun logAppLaunch(packageName: String, appName: String) {
        viewModelScope.launch {
            repository.logAppLaunch(packageName, appName)
        }
    }

    fun grantBonusScreentime(minutes: Int) {
        viewModelScope.launch {
            repository.addBonusScreentime(minutes)
        }
    }

    fun bypassLockWithParentSession() {
        _isParentBypassed.value = true
    }

    fun lockParentSession() {
        _isParentBypassed.value = false
        showParentalSettings = false
    }

    fun clearLaunchLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    private fun isBedtimeActive(settings: ParentalSettings): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val currentMinutes = hour * 60 + minute
        val startMinutes = settings.bedtimeStartHour * 60 + settings.bedtimeStartMinute
        val endMinutes = settings.bedtimeEndHour * 60 + settings.bedtimeEndMinute

        return if (startMinutes < endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else if (startMinutes > endMinutes) {
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        } else {
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
    }
}
