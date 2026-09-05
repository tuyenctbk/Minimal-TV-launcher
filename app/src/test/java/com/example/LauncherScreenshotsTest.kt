package com.example

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.example.data.LaunchLog
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class LauncherScreenshotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screenshot_screentime_lock_overlay() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FullscreenLockOverlay(
                    reason = LockReason.SCREENTIME,
                    onBypassClicked = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/screentime_lock_overlay.png")
    }

    @Test
    fun screenshot_bedtime_lock_overlay() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FullscreenLockOverlay(
                    reason = LockReason.BEDTIME,
                    onBypassClicked = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/bedtime_lock_overlay.png")
    }

    @Test
    fun screenshot_pin_pad_dialog() {
        val pinState = mutableStateOf("")
        composeTestRule.setContent {
            MyApplicationTheme {
                TvPinPadDialog(
                    title = "Parental Security Lock",
                    onPinEntered = { pinState.value = it },
                    onDismiss = {},
                    onForgotPinClicked = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/pin_pad_dialog.png")
    }

    @Test
    fun screenshot_pin_recovery_dialog() {
        composeTestRule.setContent {
            MyApplicationTheme {
                TvPinRecoveryDialog(
                    registeredEmail = "tuyenctbk@gmail.com",
                    onDismiss = {},
                    onRequestOtp = { onGenerated -> onGenerated("748291", "tuyenctbk@gmail.com") },
                    onVerifyAndReset = { _, _, _, _ -> }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/pin_recovery_dialog.png")
    }

    @Test
    fun screenshot_tv_card_preview() {
        composeTestRule.setContent {
            MyApplicationTheme {
                TvFocusableCard(onClick = {}) { isFocused ->
                    androidx.compose.material3.Text("Sample TV App", modifier = androidx.compose.ui.Modifier.padding(16.dp))
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tv_card_preview.png")
    }

    @Test
    fun screenshot_tv_card_preview_light() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                TvFocusableCard(onClick = {}) { isFocused ->
                    androidx.compose.material3.Text("Sample TV App (Light)", modifier = androidx.compose.ui.Modifier.padding(16.dp))
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tv_card_preview_light.png")
    }
}
