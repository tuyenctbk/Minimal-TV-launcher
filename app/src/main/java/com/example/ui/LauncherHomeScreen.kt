package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ParentalSettings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@Composable
fun getCategoryLabel(category: String): String {
    return when (category.lowercase(Locale.ROOT)) {
        "all" -> stringResource(R.string.category_all)
        "streaming" -> stringResource(R.string.category_streaming)
        "games" -> stringResource(R.string.category_games)
        "educational" -> stringResource(R.string.category_educational)
        "productivity" -> stringResource(R.string.category_productivity)
        else -> stringResource(R.string.category_other)
    }
}

@Composable
fun LauncherHomeScreen(
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isBypassed by viewModel.isParentBypassed.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val selectedCategory = viewModel.selectedCategory

    // Clock and Date State
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            currentDate = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
            delay(10000) // Update clock every 10 seconds
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background),
                    radius = 1200f
                )
            )
    ) {
        when (val state = uiState) {
            is LauncherUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is LauncherUiState.Success -> {
                val settings = state.settings
                val isLocked = state.isLocked && !isBypassed

                // 1. If currently locked under bedtime or screentime rules, render full-screen block overlay
                if (isLocked) {
                    FullscreenLockOverlay(
                        reason = state.lockReason,
                        onBypassClicked = {
                            viewModel.pinPromptSuccessCallback = {
                                viewModel.bypassLockWithParentSession()
                            }
                            viewModel.showPinPromptForSettings = true
                        }
                    )
                }
                // 2. If parental settings panel is requested, open settings dashboard
                else if (viewModel.showParentalSettings) {
                    ParentalSettingsScreen(
                        viewModel = viewModel,
                        onCloseSettings = {
                            viewModel.lockParentSession()
                        }
                    )
                }
                // 3. Main television dashboard
                else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // TOP STATUS BAR (Clock, Profile avatar, Screentime Tracker, settings cog)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Profile Avatar & Info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Rounded Avatar with initial L
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "L",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF818CF8)
                                        )
                                    )
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.greeting_user),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = if (isBypassed) stringResource(R.string.profile_parent) else stringResource(R.string.profile_kid),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }

                            // Right Side: Online Status, Clock, Theme Toggle & Settings
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Online Status Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)) // emerald-500
                                    )
                                    Text(
                                        text = stringResource(R.string.status_online),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                // Clock Text
                                Text(
                                    text = currentTime,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        letterSpacing = (-0.5).sp
                                    )
                                )

                                // Theme Toggle Button (Light / Dark Mode switch for living room ambient conditions)
                                var isThemeBtnFocused by remember { mutableStateOf(false) }
                                val themeBtnScale by animateFloatAsState(targetValue = if (isThemeBtnFocused) 1.15f else 1.0f)
                                val isLightMode = themeMode == "LIGHT"
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .scale(themeBtnScale)
                                        .clip(CircleShape)
                                        .background(if (isThemeBtnFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = if (isThemeBtnFocused) 2.dp else 1.dp,
                                            color = if (isThemeBtnFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .onFocusChanged { isThemeBtnFocused = it.isFocused }
                                        .focusable()
                                        .clickable {
                                            viewModel.toggleThemeMode()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isLightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = if (isLightMode) stringResource(R.string.switch_to_dark_mode) else stringResource(R.string.switch_to_light_mode),
                                        tint = if (isThemeBtnFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Settings Button
                                var isSettingsFocused by remember { mutableStateOf(false) }
                                val settingsScale by animateFloatAsState(targetValue = if (isSettingsFocused) 1.15f else 1.0f)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .scale(settingsScale)
                                        .clip(CircleShape)
                                        .background(if (isSettingsFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = if (isSettingsFocused) 2.dp else 1.dp,
                                            color = if (isSettingsFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .onFocusChanged { isSettingsFocused = it.isFocused }
                                        .focusable()
                                        .clickable {
                                            viewModel.pinPromptSuccessCallback = {
                                                viewModel.bypassLockWithParentSession()
                                                viewModel.showParentalSettings = true
                                            }
                                            viewModel.showPinPromptForSettings = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.parent_settings),
                                        tint = if (isSettingsFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Parental Supervision Active Dashboard (Elegant M3 Card representation)
                        var isDashFocused by remember { mutableStateOf(false) }
                        val dashScale by animateFloatAsState(targetValue = if (isDashFocused) 1.02f else 1.0f)
                        val totalLimitMinutes = settings.dailyLimitMinutes
                        val totalLimitMs = totalLimitMinutes * 60 * 1000L
                        val remainingMs = state.remainingScreentimeMs
                        val progress = if (totalLimitMinutes > 0) (remainingMs.toFloat() / totalLimitMs).coerceIn(0f, 1f) else 1f
                        val remainingText = when {
                            isBypassed -> stringResource(R.string.unlimited_session)
                            totalLimitMinutes == 0 -> stringResource(R.string.unlimited_play)
                            else -> {
                                val remainingMins = (remainingMs / (60 * 1000L)).toInt()
                                val remainingSecs = ((remainingMs % (60 * 1000L)) / 1000L).toInt()
                                if (remainingMins > 0) stringResource(R.string.remaining_mins_short, remainingMins) else stringResource(R.string.remaining_secs_short, remainingSecs)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(dashScale)
                                .onFocusChanged { isDashFocused = it.isFocused }
                                .focusable()
                                .clickable {
                                    if (totalLimitMinutes > 0 && !isBypassed) {
                                        viewModel.pinPromptSuccessCallback = {
                                            viewModel.grantBonusScreentime(15) // Grant 15 mins bonus
                                            Toast.makeText(context, context.getString(R.string.toast_added_screentime), Toast.LENGTH_SHORT).show()
                                        }
                                        viewModel.showPinPromptForSettings = true
                                    }
                                }
                                .border(
                                    width = if (isDashFocused) 2.dp else 1.dp,
                                    color = if (isDashFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isBypassed) stringResource(R.string.supervisor_override_active) else stringResource(R.string.supervision_active),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = remainingText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }

                                // Elegant smooth linear progress bar
                                if (totalLimitMinutes > 0 && !isBypassed) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(progress)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                                    )
                                                )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(
                                                R.string.next_break_bedtime,
                                                settings.bedtimeStartHour,
                                                settings.bedtimeStartMinute
                                            ),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                        Text(
                                            text = stringResource(R.string.managed_by_parental_dashboard),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        )
                                    }

                                    if (totalLimitMinutes > 0 && !isBypassed) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.btn_ask_more_time),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    letterSpacing = 1.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val calendar = Calendar.getInstance()
                        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                        val currentMinute = calendar.get(Calendar.MINUTE)
                        val currentTotalMins = currentHour * 60 + currentMinute
                        val bedtimeStartTotalMins = settings.bedtimeStartHour * 60 + settings.bedtimeStartMinute
                        val minutesUntilBedtime = if (bedtimeStartTotalMins >= currentTotalMins) {
                            bedtimeStartTotalMins - currentTotalMins
                        } else {
                            (24 * 60 - currentTotalMins) + bedtimeStartTotalMins
                        }

                        val showBedtimeWarning = minutesUntilBedtime in 1..15 && !isLocked

                        if (showBedtimeWarning) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = stringResource(R.string.bedtime_warning_cd),
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(
                                                R.string.bedtime_warning_title,
                                                minutesUntilBedtime,
                                                if (minutesUntilBedtime > 1) "s" else ""
                                            ),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = stringResource(R.string.bedtime_warning_subtitle),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        )

                        // Search box for TV launcher
                        var isSearchFocused by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(stringResource(R.string.search_apps_placeholder), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search), tint = Color.LightGray)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSearchFocused = it.isFocused }
                                .border(
                                    width = if (isSearchFocused) 2.dp else 1.dp,
                                    color = if (isSearchFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )

                        // CATEGORY FILTER BAR
                        val categories = listOf("All", "Streaming", "Games", "Educational", "Productivity", "Other")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                                var isChipFocused by remember { mutableStateOf(false) }
                                val chipScale by animateFloatAsState(targetValue = if (isChipFocused) 1.08f else 1.0f)

                                val chipBg = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isChipFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                }
                                val chipTextColor = when {
                                    isSelected -> Color.White
                                    isChipFocused -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                }

                                Row(
                                    modifier = Modifier
                                        .scale(chipScale)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(chipBg)
                                        .border(
                                            width = if (isChipFocused) 2.dp else 1.dp,
                                            color = if (isChipFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .onFocusChanged { isChipFocused = it.isFocused }
                                        .focusable()
                                        .clickable {
                                            viewModel.selectCategory(cat)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val catIcon = when (cat) {
                                        "Streaming" -> Icons.Default.Tv
                                        "Games" -> Icons.Default.SportsEsports
                                        "Educational" -> Icons.Default.School
                                        "Productivity" -> Icons.Default.CheckCircle
                                        "Other" -> Icons.Default.Category
                                        else -> Icons.Default.GridView
                                    }
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        tint = chipTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = getCategoryLabel(cat),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = chipTextColor
                                        )
                                    )
                                }
                            }
                        }

                        // FILTER & HERO ROW
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBypassed) stringResource(R.string.your_favorites_parent) else stringResource(R.string.your_favorites),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    letterSpacing = 1.5.sp
                                ),
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )

                            if (isBypassed) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text(text = stringResource(R.string.unrestricted_session), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary))
                                }
                            }
                        }

                        // Filter out blocked apps if in Kids Mode and match category + search
                        val visibleApps = (if (isBypassed) {
                            state.apps
                        } else {
                            state.apps.filter { !it.isBlocked }
                        }).filter { app ->
                            app.appName.contains(searchQuery, ignoreCase = true) &&
                            (selectedCategory == "All" || app.category.equals(selectedCategory, ignoreCase = true))
                        }

                        if (visibleApps.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AppsOutage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.no_apps_found_title),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = stringResource(R.string.no_apps_found_desc),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // APPLICATIONS GRID (Adaptive, horizontal scrolling layout optimized for TV viewport)
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 140.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                items(visibleApps) { app ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.width(140.dp)
                                    ) {
                                        TvFocusableCard(
                                            onClick = {
                                                if (app.isBlocked && !isBypassed) {
                                                    // Require parent PIN to bypass blocked app
                                                    viewModel.pinPromptSuccessCallback = {
                                                        launchApp(context, app, viewModel)
                                                    }
                                                    viewModel.showPinPromptForSettings = true
                                                } else {
                                                    launchApp(context, app, viewModel)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(110.dp),
                                            testTag = "app_item_${app.packageName}"
                                        ) { isFocused ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Image(
                                                    bitmap = drawableToImageBitmap(app.icon),
                                                    contentDescription = app.appName,
                                                    modifier = Modifier.size(52.dp)
                                                )
                                            }

                                            // Draw restricted small icon badge if blocked
                                            if (app.isBlocked) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(6.dp)
                                                        .size(20.dp)
                                                        .background(AccentOrange, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = stringResource(R.string.app_blocked_badge),
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = app.appName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                        )

                                        Text(
                                            text = getCategoryLabel(app.category),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Secure 4-digit PIN pad prompt overlay
        if (viewModel.showPinPromptForSettings) {
            TvPinPadDialog(
                title = stringResource(R.string.pin_dialog_title_auth),
                onDismiss = {
                    viewModel.showPinPromptForSettings = false
                    viewModel.pinPromptSuccessCallback = null
                },
                onPinEntered = { pin ->
                    viewModel.verifyPin(
                        enteredPin = pin,
                        onCorrect = {
                            viewModel.showPinPromptForSettings = false
                            viewModel.pinPromptSuccessCallback?.invoke()
                            viewModel.pinPromptSuccessCallback = null
                        },
                        onIncorrect = {
                            Toast.makeText(context, context.getString(R.string.toast_invalid_pin), Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onForgotPinClicked = {
                    viewModel.showPinPromptForSettings = false
                    viewModel.showPinRecoveryDialog = true
                }
            )
        }

        // 5. TV PIN Recovery Dialog via Registered Email
        if (viewModel.showPinRecoveryDialog) {
            val registeredEmail = (uiState as? LauncherUiState.Success)?.settings?.recoveryEmail?.ifEmpty { "tuyenctbk@gmail.com" } ?: "tuyenctbk@gmail.com"
            TvPinRecoveryDialog(
                registeredEmail = registeredEmail,
                onDismiss = {
                    viewModel.showPinRecoveryDialog = false
                },
                onRequestOtp = { onGenerated ->
                    viewModel.sendRecoveryOtp(onGenerated)
                },
                onVerifyAndReset = { otp, newPin, onSuccess, onError ->
                    viewModel.verifyRecoveryOtpAndResetPin(
                        otp = otp,
                        newPin = newPin,
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.toast_pin_reset_success), Toast.LENGTH_LONG).show()
                            viewModel.showPinRecoveryDialog = false
                            onSuccess()
                        },
                        onFailed = { _ ->
                            onError(context.getString(R.string.recovery_err_invalid_otp))
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun ScreentimeStatusCard(
    settings: ParentalSettings,
    remainingMs: Long,
    isBypassed: Boolean,
    onExtendClicked: () -> Unit
) {
    var isPillFocused by remember { mutableStateOf(false) }

    val containerBg = if (isBypassed) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(containerBg)
            .border(
                width = if (isPillFocused) 2.dp else 1.dp,
                color = if (isPillFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(27.dp)
            )
            .onFocusChanged { isPillFocused = it.isFocused }
            .focusable()
            .clickable {
                if (settings.dailyLimitMinutes > 0 && !isBypassed) {
                    onExtendClicked()
                }
            }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        val icon = if (isBypassed) Icons.Default.SupervisorAccount else Icons.Default.Shield
        val color = if (isBypassed) MaterialTheme.colorScheme.primary else AccentGreen

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )

        Column {
            Text(
                text = if (isBypassed) stringResource(R.string.supervisor_mode) else stringResource(R.string.remaining_screentime),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            )

            val text = when {
                isBypassed -> stringResource(R.string.unlimited_session)
                settings.dailyLimitMinutes == 0 -> stringResource(R.string.unlimited_play)
                else -> {
                    val remainingMins = (remainingMs / (60 * 1000L)).toInt()
                    val remainingSecs = ((remainingMs % (60 * 1000L)) / 1000L).toInt()
                    if (remainingMins > 0) stringResource(R.string.remaining_mins, remainingMins) else stringResource(R.string.remaining_secs, remainingSecs)
                }
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // If child screentime has a countdown limit, display a quick "+15" pill parents can click easily
        if (settings.dailyLimitMinutes > 0 && !isBypassed) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "+15m",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentOrange
                    )
                )
            }
        }
    }
}

private fun launchApp(
    context: Context,
    app: LauncherAppItem,
    viewModel: LauncherViewModel
) {
    try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            viewModel.logAppLaunch(app.packageName, app.appName)
        } else {
            Toast.makeText(context, context.getString(R.string.toast_could_not_open_app, app.appName), Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.toast_launch_failed, e.message ?: ""), Toast.LENGTH_SHORT).show()
    }
}

