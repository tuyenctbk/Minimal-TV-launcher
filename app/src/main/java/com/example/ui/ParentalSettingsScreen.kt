package com.example.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.R
import com.example.data.AppRestriction
import com.example.data.LaunchLog
import com.example.data.ParentalSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ParentalSettingsScreen(
    viewModel: LauncherViewModel,
    onCloseSettings: () -> Unit
) {
    var activeTab by remember { mutableStateOf(ParentalTab.APP_RESTRICTIONS) }

    val uiState by viewModel.uiState.collectAsState()

    if (uiState is LauncherUiState.Success) {
        val successState = uiState as LauncherUiState.Success
        val settings = successState.settings
        val recentLogs = successState.recentLogs
        val restrictions = successState.restrictions
        val apps = successState.apps

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // LEFT COLUMN: Navigation Tabs
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.settings_header_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Tabs List
                ParentalTab.entries.forEach { tab ->
                    var isTabFocused by remember { mutableStateOf(false) }
                    val isSelected = activeTab == tab
                    val tabBg = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isTabFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isSelected -> Color.White
                        isTabFocused -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tabBg)
                            .onFocusChanged { isTabFocused = it.isFocused }
                            .focusable()
                            .clickable { activeTab = tab }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(tab.titleRes),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Exit settings tab button
                var isExitFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExitFocused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .onFocusChanged { isExitFocused = it.isFocused }
                        .focusable()
                        .clickable { onCloseSettings() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isExitFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.btn_exit_settings),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isExitFocused) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // RIGHT COLUMN: Content Panel matching selected tab
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(24.dp)
            ) {
                AnimatedContent(targetState = activeTab) { tab ->
                    when (tab) {
                        ParentalTab.APP_RESTRICTIONS -> {
                            AppRestrictionsTab(apps = apps, viewModel = viewModel)
                        }
                        ParentalTab.SCREENTIME_LIMIT -> {
                            ScreentimeLimitsTab(settings = settings, viewModel = viewModel)
                        }
                        ParentalTab.BEDTIME_SCHEDULE -> {
                            BedtimeScheduleTab(settings = settings, viewModel = viewModel)
                        }
                        ParentalTab.THEME_APPEARANCE -> {
                            ThemeAppearanceTab(settings = settings, viewModel = viewModel)
                        }
                        ParentalTab.SUPERVISION_LOGS -> {
                            SupervisionLogsTab(recentLogs = recentLogs, viewModel = viewModel)
                        }
                        ParentalTab.PIN_CONFIGURATION -> {
                            PinConfigurationTab(settings = settings, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

enum class ParentalTab(@StringRes val titleRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    APP_RESTRICTIONS(R.string.tab_app_guard, Icons.Default.Block),
    SCREENTIME_LIMIT(R.string.tab_screentime, Icons.Default.Timer),
    BEDTIME_SCHEDULE(R.string.tab_bedtime, Icons.Default.Bedtime),
    THEME_APPEARANCE(R.string.tab_theme, Icons.Default.Palette),
    SUPERVISION_LOGS(R.string.tab_activity, Icons.AutoMirrored.Filled.List),
    PIN_CONFIGURATION(R.string.tab_security, Icons.Default.Lock)
}

/**
 * Tab to manage app blocklists. Lists all queried standard and leanback apps.
 */
@Composable
fun AppRestrictionsTab(
    apps: List<LauncherAppItem>,
    viewModel: LauncherViewModel
) {
    var selectedFilterCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Streaming", "Games", "Educational", "Productivity", "Other")

    val filteredApps = if (selectedFilterCategory == "All") {
        apps
    } else {
        apps.filter { it.category.equals(selectedFilterCategory, ignoreCase = true) }
    }

    val availableCategories = listOf("Streaming", "Games", "Educational", "Productivity", "Other")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.app_guard_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        )
        Text(
            text = stringResource(R.string.app_guard_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Category Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { cat ->
                var isCatFocused by remember { mutableStateOf(false) }
                val isSelected = selectedFilterCategory == cat
                val chipBg = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isCatFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }

                val catLabel = when (cat) {
                    "All" -> stringResource(R.string.category_all)
                    "Streaming" -> stringResource(R.string.category_streaming)
                    "Games" -> stringResource(R.string.category_games)
                    "Educational" -> stringResource(R.string.category_educational)
                    "Productivity" -> stringResource(R.string.category_productivity)
                    else -> stringResource(R.string.category_other)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipBg)
                        .border(
                            width = if (isCatFocused || isSelected) 1.5.dp else 1.dp,
                            color = if (isCatFocused) Color.White else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .onFocusChanged { isCatFocused = it.isFocused }
                        .focusable()
                        .clickable { selectedFilterCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = catLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected || isCatFocused) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        // Quick batch action buttons (e.g. Block Games / Allow Educational)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isBlockGamesFocused by remember { mutableStateOf(false) }
            val anyGamesBlocked = apps.any { it.category.equals("Games", ignoreCase = true) && it.isBlocked }

            Button(
                onClick = {
                    viewModel.setCategoryBlocked("Games", !anyGamesBlocked)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBlockGamesFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .onFocusChanged { isBlockGamesFocused = it.isFocused }
                    .border(
                        width = 1.dp,
                        color = if (isBlockGamesFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = if (isBlockGamesFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (anyGamesBlocked) stringResource(R.string.btn_unblock_all_games) else stringResource(R.string.btn_block_all_games),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isBlockGamesFocused) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            var isAllowEduFocused by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    viewModel.setCategoryBlocked("Educational", false)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAllowEduFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .onFocusChanged { isAllowEduFocused = it.isFocused }
                    .border(
                        width = 1.dp,
                        color = if (isAllowEduFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = if (isAllowEduFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_allow_all_educational),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isAllowEduFocused) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                var isRowFocused by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isRowFocused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = if (isRowFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .onFocusChanged { isRowFocused = it.isFocused }
                        .focusable()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = drawableToImageBitmap(app.icon),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Clickable Category Badge that cycles categories when clicked
                    var isBadgeFocused by remember { mutableStateOf(false) }
                    val currentCat = app.category.ifEmpty { "Other" }
                    val catColor = when (currentCat) {
                        "Games" -> AccentOrange
                        "Educational" -> AccentGreen
                        "Streaming" -> MaterialTheme.colorScheme.primary
                        "Productivity" -> Color(0xFF0EA5E9)
                        else -> Color(0xFF8B5CF6)
                    }

                    val currentCatLabel = when (currentCat) {
                        "Streaming" -> stringResource(R.string.category_streaming)
                        "Games" -> stringResource(R.string.category_games)
                        "Educational" -> stringResource(R.string.category_educational)
                        "Productivity" -> stringResource(R.string.category_productivity)
                        else -> stringResource(R.string.category_other)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(catColor.copy(alpha = if (isBadgeFocused) 0.35f else 0.15f))
                            .border(1.dp, if (isBadgeFocused) Color.White else catColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .onFocusChanged { isBadgeFocused = it.isFocused }
                            .focusable()
                            .clickable {
                                // Cycle to next category
                                val currentIndex = availableCategories.indexOf(currentCat)
                                val nextIndex = (currentIndex + 1).rem(availableCategories.size)
                                val nextCategory = availableCategories[nextIndex]
                                viewModel.updateAppCategory(app.packageName, nextCategory)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (currentCat) {
                                    "Games" -> Icons.Default.SportsEsports
                                    "Educational" -> Icons.Default.School
                                    "Streaming" -> Icons.Default.Tv
                                    "Productivity" -> Icons.Default.Work
                                    else -> Icons.Default.Category
                                },
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = currentCatLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = catColor
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Switch(
                        checked = app.isBlocked,
                        onCheckedChange = { viewModel.toggleAppBlocked(app.packageName, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentOrange,
                            checkedTrackColor = AccentOrange.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Tab to manage total daily screentime.
 */
@Composable
fun ScreentimeLimitsTab(
    settings: ParentalSettings,
    viewModel: LauncherViewModel
) {
    val limits = listOf(0, 15, 30, 45, 60, 90, 120, 180, 240) // minutes. 0 = unlimited

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.screentime_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        )
        Text(
            text = stringResource(R.string.screentime_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f))
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.screentime_current_limit),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )
                Text(
                    text = if (settings.dailyLimitMinutes > 0) stringResource(R.string.screentime_limit_minutes, settings.dailyLimitMinutes) else stringResource(R.string.screentime_unlimited_play),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )
            }
        }

        Text(
            text = stringResource(R.string.screentime_select_duration),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(limits, key = { it }) { minutes ->
                var isItemFocused by remember { mutableStateOf(false) }
                val isSelected = settings.dailyLimitMinutes == minutes

                val bg = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isItemFocused -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                }

                val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(
                            width = 1.dp,
                            color = if (isItemFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .onFocusChanged { isItemFocused = it.isFocused }
                        .focusable()
                        .clickable { viewModel.setDailyLimit(minutes) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (minutes == 0) Icons.Default.AllInclusive else Icons.Default.Timer,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (minutes == 0) stringResource(R.string.screentime_no_limits) else stringResource(R.string.screentime_limit_minutes, minutes),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = contentColor),
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Tab to manage bedtime / black-out hours.
 */
@Composable
fun BedtimeScheduleTab(
    settings: ParentalSettings,
    viewModel: LauncherViewModel
) {
    // Bedtime options: standard start hours (5 PM to 1 AM) and end hours (4 AM to 11 AM)
    val startHours = listOf(17, 18, 19, 20, 21, 22, 23, 0, 1)
    val endHours = listOf(4, 5, 6, 7, 8, 9, 10, 11)

    val displayStartHr = if (settings.bedtimeStartHour > 12) settings.bedtimeStartHour - 12 else (if (settings.bedtimeStartHour == 0) 12 else settings.bedtimeStartHour)
    val startAmPm = if (settings.bedtimeStartHour in 12..23) "PM" else "AM"

    val displayEndHr = if (settings.bedtimeEndHour > 12) settings.bedtimeEndHour - 12 else (if (settings.bedtimeEndHour == 0) 12 else settings.bedtimeEndHour)
    val endAmPm = if (settings.bedtimeEndHour in 12..23) "PM" else "AM"

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.bedtime_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        )
        Text(
            text = stringResource(R.string.bedtime_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Start Bedtime Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(R.string.bedtime_start_hour_label), style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                Text(
                    text = "$displayStartHr:00 $startAmPm",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )

                LazyColumn(modifier = Modifier.height(200.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(startHours, key = { it }) { hr ->
                        var isFocused by remember { mutableStateOf(false) }
                        val isSelected = settings.bedtimeStartHour == hr
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else (if (isFocused) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        val textCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                        val itemHr = if (hr > 12) hr - 12 else (if (hr == 0) 12 else hr)
                        val itemAmPm = if (hr in 12..23) "PM" else "AM"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .border(
                                    1.dp,
                                    if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .onFocusChanged { isFocused = it.isFocused }
                                .focusable()
                                .clickable { viewModel.setBedtimeSchedule(hr, settings.bedtimeEndHour) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$itemHr:00 $itemAmPm", color = textCol, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // End Bedtime Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(R.string.bedtime_end_hour_label), style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                Text(
                    text = "$displayEndHr:00 $endAmPm",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                )

                LazyColumn(modifier = Modifier.height(200.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(endHours, key = { it }) { hr ->
                        var isFocused by remember { mutableStateOf(false) }
                        val isSelected = settings.bedtimeEndHour == hr
                        val bg = if (isSelected) Color(0xFF10B981) else (if (isFocused) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        val textCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                        val itemHr = if (hr > 12) hr - 12 else (if (hr == 0) 12 else hr)
                        val itemAmPm = if (hr in 12..23) "PM" else "AM"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .border(
                                    1.dp,
                                    if (isFocused) Color(0xFF10B981) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .onFocusChanged { isFocused = it.isFocused }
                                .focusable()
                                .clickable { viewModel.setBedtimeSchedule(settings.bedtimeStartHour, hr) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$itemHr:00 $itemAmPm", color = textCol, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab to review parental monitoring logs.
 */
@Composable
fun SupervisionLogsTab(
    recentLogs: List<LaunchLog>,
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val usageTodayMin = if (uiState is LauncherUiState.Success) {
        val settings = (uiState as LauncherUiState.Success).settings
        (settings.usageTodayMs / (60 * 1000L)).toInt()
    } else {
        0
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.logs_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = stringResource(R.string.logs_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                var isResetUsageFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        viewModel.resetTodayUsage()
                        Toast.makeText(context, context.getString(R.string.toast_screentime_reset), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isResetUsageFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .onFocusChanged { isResetUsageFocused = it.isFocused }
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = if (isResetUsageFocused) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.btn_reset_screentime),
                        color = if (isResetUsageFocused) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                var isClearFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        viewModel.clearLaunchLogs()
                        Toast.makeText(context, context.getString(R.string.toast_logs_cleared), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isClearFocused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .onFocusChanged { isClearFocused = it.isFocused }
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = if (isClearFocused) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.btn_clear_all_logs),
                        color = if (isClearFocused) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // STATS INSIGHTS ROW (Visual Progress Bars & Metrics)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Screentime Usage Summary
            Card(
                modifier = Modifier.weight(1f).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.logs_card_today_screentime),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.logs_stat_mins, usageTodayMin),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = stringResource(R.string.logs_card_today_screentime_sub),
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }

            // Card 2: App Launch Frequency Distribution (Mini-Bar-Chart)
            Card(
                modifier = Modifier.weight(1.5f).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.logs_card_frequency_chart),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    )

                    val topApps = recentLogs.groupBy { it.appName }
                        .mapValues { it.value.size }
                        .toList()
                        .sortedByDescending { it.second }
                        .take(3)

                    if (topApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(R.string.logs_no_stats), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        val maxLaunches = topApps.maxOf { it.second }.toFloat()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            topApps.forEach { (appName, count) ->
                                val pct = count.toFloat() / maxLaunches
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = appName, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold))
                                        Text(text = stringResource(R.string.logs_launch_count, count), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(pct)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Activity Logs List Section
        Text(
            text = stringResource(R.string.logs_chronological_header),
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            modifier = Modifier.padding(top = 8.dp)
        )

        if (recentLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.HistoryToggleOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                    Text(text = stringResource(R.string.logs_no_logs_yet), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(recentLogs, key = { it.id }) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Launch, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = log.appName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                            Text(text = log.packageName, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                        }
                        Text(
                            text = SimpleDateFormat("MMM dd, hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        var isDeleteLogFocused by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                viewModel.deleteLaunchLog(log.id)
                                Toast.makeText(context, context.getString(R.string.toast_log_deleted), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .onFocusChanged { isDeleteLogFocused = it.isFocused }
                                .background(if (isDeleteLogFocused) MaterialTheme.colorScheme.error else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.btn_delete_log),
                                tint = if (isDeleteLogFocused) Color.White else MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab to configure theme and ambient display settings.
 */
@Composable
fun ThemeAppearanceTab(
    settings: ParentalSettings,
    viewModel: LauncherViewModel
) {
    val themeMode by viewModel.themeMode.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.theme_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        )
        Text(
            text = stringResource(R.string.theme_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dark Mode Card
            var isDarkFocused by remember { mutableStateOf(false) }
            val isDarkSelected = themeMode != "LIGHT"
            Card(
                onClick = { viewModel.setThemeMode("DARK") },
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = if (isDarkFocused || isDarkSelected) 2.dp else 1.dp,
                        color = if (isDarkFocused) Color.White else if (isDarkSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .onFocusChanged { isDarkFocused = it.isFocused }
                    .focusable(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkFocused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFF818CF8))
                        }
                        if (isDarkSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.selected_cd), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(
                        text = stringResource(R.string.theme_dark_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = stringResource(R.string.theme_dark_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    )
                }
            }

            // Light Mode Card
            var isLightFocused by remember { mutableStateOf(false) }
            val isLightSelected = themeMode == "LIGHT"
            Card(
                onClick = { viewModel.setThemeMode("LIGHT") },
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = if (isLightFocused || isLightSelected) 2.dp else 1.dp,
                        color = if (isLightFocused) Color.White else if (isLightSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .onFocusChanged { isLightFocused = it.isFocused }
                    .focusable(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLightFocused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LightMode, contentDescription = null, tint = Color(0xFFF59E0B))
                        }
                        if (isLightSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.selected_cd), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(
                        text = stringResource(R.string.theme_light_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = stringResource(R.string.theme_light_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    )
                }
            }
        }
    }
}

/**
 * Tab to change parental secure PIN and configure email recovery.
 */
@Composable
fun PinConfigurationTab(
    settings: ParentalSettings,
    viewModel: LauncherViewModel
) {
    var pinText by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var errMsg by remember { mutableStateOf("") }

    var profileNameInput by remember { mutableStateOf(settings.profileName.ifEmpty { "Family" }) }
    var profileSuccessMsg by remember { mutableStateOf("") }

    var recoveryEmailInput by remember { mutableStateOf(settings.recoveryEmail.ifEmpty { "parent@home.com" }) }
    var emailSuccessMsg by remember { mutableStateOf("") }
    var simulatedOtpCode by remember { mutableStateOf("") }

    val pinSavedMsg = stringResource(R.string.pin_saved_success)
    val pinLengthMsg = stringResource(R.string.recovery_err_pin_length)
    val otpValidMsg = stringResource(R.string.recovery_code_generated_valid)
    val profileSavedMsg = stringResource(R.string.toast_profile_saved)
    val emailSavedMsg = stringResource(R.string.toast_email_saved)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // LIVING ROOM PROFILE NAME SECTION
        item {
            Text(
                text = stringResource(R.string.profile_name_label),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            )
            Text(
                text = stringResource(R.string.profile_name_desc),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = profileNameInput,
                    onValueChange = {
                        profileNameInput = it
                        profileSuccessMsg = ""
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )

                var isSaveProfileFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (profileNameInput.isNotBlank()) {
                            viewModel.setProfileName(profileNameInput.trim())
                            profileSuccessMsg = profileSavedMsg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSaveProfileFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .onFocusChanged { isSaveProfileFocused = it.isFocused }
                        .border(1.dp, if (isSaveProfileFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = stringResource(R.string.profile_name_save),
                        color = if (isSaveProfileFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (profileSuccessMsg.isNotEmpty()) {
                Text(text = profileSuccessMsg, color = AccentGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }

        // PIN MANAGEMENT SECTION
        item {
            Text(
                text = stringResource(R.string.pin_mgmt_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            )
            Text(
                text = stringResource(R.string.pin_mgmt_desc),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.pin_current_protection),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                    Text(
                        text = settings.pin.map { "*" }.joinToString(""),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.pin_enter_4_digits_change),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Custom PIN dots representation
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val active = i < pinText.length
                    val dotColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(dotColor)
                    )
                }
            }

            if (successMsg.isNotEmpty()) {
                Text(text = successMsg, color = AccentGreen, style = MaterialTheme.typography.bodyMedium)
            }
            if (errMsg.isNotEmpty()) {
                Text(text = errMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "Delete")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.width(320.dp)
            ) {
                keys.forEach { rowKeys ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        rowKeys.forEach { key ->
                            var isFocused by remember { mutableStateOf(false) }
                            val bg = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bg)
                                    .border(1.dp, if (isFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .focusable()
                                    .clickable {
                                        successMsg = ""
                                        errMsg = ""
                                        when (key) {
                                            "Clear" -> pinText = ""
                                            "Delete" -> if (pinText.isNotEmpty()) pinText = pinText.dropLast(1)
                                            else -> {
                                                if (pinText.length < 4) {
                                                    pinText += key
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "Delete") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = stringResource(R.string.pin_key_delete),
                                        tint = if (isFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = if (key == "Clear") stringResource(R.string.pin_key_clear) else key,
                                        color = if (isFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                var isSavePinFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (pinText.length == 4) {
                            viewModel.setParentPin(pinText)
                            successMsg = pinSavedMsg
                            pinText = ""
                        } else {
                            errMsg = pinLengthMsg
                        }
                    },
                    enabled = pinText.length == 4,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSavePinFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isSavePinFocused = it.isFocused }
                        .border(
                            1.dp,
                            if (isSavePinFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = stringResource(R.string.recovery_btn_save),
                        color = if (pinText.length == 4) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // SECURE RECOVERY MECHANISM SECTION
        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.recovery_mgmt_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            )
            Text(
                text = stringResource(R.string.recovery_mgmt_desc),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f))
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recovery_registered_address_label),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = recoveryEmailInput,
                            onValueChange = {
                                recoveryEmailInput = it
                                emailSuccessMsg = ""
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        var isSaveEmailFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                if (recoveryEmailInput.contains("@")) {
                                    viewModel.setRecoveryEmail(recoveryEmailInput.trim())
                                    emailSuccessMsg = emailSavedMsg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSaveEmailFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .onFocusChanged { isSaveEmailFocused = it.isFocused }
                                .border(1.dp, if (isSaveEmailFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = stringResource(R.string.recovery_email_save),
                                color = if (isSaveEmailFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        var isTestSendFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                viewModel.sendRecoveryOtp { code, _ ->
                                    simulatedOtpCode = code
                                    emailSuccessMsg = otpValidMsg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTestSendFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .onFocusChanged { isTestSendFocused = it.isFocused }
                                .border(1.dp, if (isTestSendFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isTestSendFocused) Color.White else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.btn_test_recovery_dispatch),
                                color = if (isTestSendFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (simulatedOtpCode.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.recovery_simulated_dispatch_title),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = stringResource(R.string.recovery_simulated_code_label, simulatedOtpCode),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                )
                                Text(
                                    text = stringResource(R.string.recovery_simulated_help),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }

                    if (emailSuccessMsg.isNotEmpty()) {
                        Text(text = emailSuccessMsg, color = AccentGreen, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
