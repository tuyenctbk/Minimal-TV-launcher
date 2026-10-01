package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R

// Premium gradient colors for the Elegant Dark TV atmosphere
val CinematicDarkBg = Color(0xFF09090B)       // zinc-950
val CinematicDarkSurface = Color(0xFF18181B)  // zinc-900
val CinematicBlueGlow = Color(0xFF312E81)     // Indigo-900
val CinematicVioletGlow = Color(0xFF4F46E5)   // Indigo-600
val AccentOrange = Color(0xFF6366F1)           // Indigo-500 branding
val AccentGreen = Color(0xFF10B981)            // Emerald-500

// Helper function to safely convert standard drawable to ImageBitmap for Compose Image
fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap.asImageBitmap()
    }
    val width = drawable.intrinsicWidth.coerceAtLeast(120)
    val height = drawable.intrinsicHeight.coerceAtLeast(120)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap.asImageBitmap()
}

/**
 * TV Focusable Card which scales up and shows a thick high-contrast border
 * when selected, ideal for remote control focus indicators.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String = "",
    content: @Composable BoxScope.(Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.08f else 1.0f)
    val borderColors = if (isFocused) {
        listOf(MaterialTheme.colorScheme.primary, AccentOrange)
    } else {
        listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    }

    Card(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 3.dp else 1.dp,
                brush = Brush.horizontalGradient(borderColors),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .focusable(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 12.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content(isFocused)
        }
    }
}

/**
 * Custom TV-friendly Numeric PIN Entry Dialog.
 * Uses a grid layout that can be navigated with remote controllers (D-pad).
 */
@Composable
fun TvPinPadDialog(
    title: String = "Enter Parental PIN",
    expectedLength: Int = 4,
    onDismiss: () -> Unit,
    onPinEntered: (String) -> Unit,
    onForgotPinClicked: (() -> Unit)? = null
) {
    var enteredText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(440.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                // Entered dots representation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until expectedLength) {
                        val active = i < enteredText.length
                        val dotColor by animateColorAsState(
                            targetValue = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3x4 layout for digits, back, and clear
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("Clear", "0", "Delete")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    keys.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowKeys.forEach { key ->
                                var isBtnFocused by remember { mutableStateOf(false) }
                                val btnScale by animateFloatAsState(targetValue = if (isBtnFocused) 1.1f else 1.0f)
                                val btnBg = if (isBtnFocused) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                }
                                val btnTextCol = if (isBtnFocused) {
                                    Color.White
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .scale(btnScale)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(btnBg)
                                        .border(
                                            width = if (isBtnFocused) 2.dp else 1.dp,
                                            color = if (isBtnFocused) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .onFocusChanged { isBtnFocused = it.isFocused }
                                        .focusable()
                                        .clickable {
                                            when (key) {
                                                "Clear" -> {
                                                    enteredText = ""
                                                    errorMessage = ""
                                                }
                                                "Delete" -> {
                                                    if (enteredText.isNotEmpty()) {
                                                        enteredText = enteredText.dropLast(1)
                                                    }
                                                    errorMessage = ""
                                                }
                                                else -> {
                                                    if (enteredText.length < expectedLength) {
                                                        enteredText += key
                                                        errorMessage = ""
                                                    }
                                                    if (enteredText.length == expectedLength) {
                                                        onPinEntered(enteredText)
                                                        enteredText = "" // Reset for retry/next use
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
                                            tint = btnTextCol
                                        )
                                    } else {
                                        Text(
                                            text = if (key == "Clear") stringResource(R.string.pin_key_clear) else key,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = btnTextCol
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Forgot PIN action button
                if (onForgotPinClicked != null) {
                    var isForgotFocused by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isForgotFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                width = if (isForgotFocused) 1.dp else 0.dp,
                                color = if (isForgotFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { isForgotFocused = it.isFocused }
                            .focusable()
                            .clickable { onForgotPinClicked() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.forgot_pin_btn),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Explicit D-pad friendly cancel button
                var isCancelFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCancelFocused) MaterialTheme.colorScheme.error else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isCancelFocused = it.isFocused }
                        .border(
                            width = 1.dp,
                            color = if (isCancelFocused) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = stringResource(R.string.btn_cancel),
                        color = if (isCancelFocused) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * TV-Friendly PIN Recovery Dialog using registered email address.
 * Generates an OTP and allows the user to securely set a new 4-digit PIN.
 */
@Composable
fun TvPinRecoveryDialog(
    registeredEmail: String,
    onDismiss: () -> Unit,
    onRequestOtp: (onGenerated: (code: String, email: String) -> Unit) -> Unit,
    onVerifyAndReset: (otp: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Request, 2 = Enter OTP, 3 = New PIN
    var simulatedCode by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var newPinText by remember { mutableStateOf("") }
    var confirmPinText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val defaultEmailLabel = stringResource(R.string.recovery_registered_address_label)
    val codeDispatchedMsg = stringResource(R.string.recovery_code_generated_valid)

    val maskedEmail = remember(registeredEmail, defaultEmailLabel) {
        if (registeredEmail.contains("@")) {
            val parts = registeredEmail.split("@")
            val name = parts[0]
            val domain = parts[1]
            val maskedName = if (name.length > 2) name.take(2) + "***" else name + "***"
            "$maskedName@$domain"
        } else {
            defaultEmailLabel
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(480.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = when (step) {
                        1 -> stringResource(R.string.recovery_step_1_title)
                        2 -> stringResource(R.string.recovery_step_2_title)
                        else -> stringResource(R.string.recovery_step_3_title)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                when (step) {
                    1 -> {
                        Text(
                            text = stringResource(R.string.recovery_step_1_desc, maskedEmail),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            ),
                            textAlign = TextAlign.Center
                        )

                        var isSendFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                onRequestOtp { code, _ ->
                                    simulatedCode = code
                                    step = 2
                                    statusMessage = codeDispatchedMsg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSendFocused = it.isFocused }
                                .border(
                                    width = if (isSendFocused) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text(stringResource(R.string.recovery_btn_send_code), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    2 -> {
                        // Display notification simulation for TV living room testing
                        if (simulatedCode.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(R.string.recovery_email_sent_to, maskedEmail),
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = stringResource(R.string.recovery_simulated_code, simulatedCode),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        }

                        // 6-digit boxes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 6) {
                                val digit = enteredOtp.getOrNull(i)?.toString() ?: ""
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (i == enteredOtp.length) 2.dp else 1.dp,
                                            color = if (i == enteredOtp.length) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = digit,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        }

                        if (errorMessage.isNotEmpty()) {
                            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        // Compact keypad
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("Clear", "0", "Delete")
                        )

                        val mismatchErrMsg = stringResource(R.string.recovery_err_code_mismatch)

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            keys.forEach { rowKeys ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    rowKeys.forEach { key ->
                                        var isFocused by remember { mutableStateOf(false) }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                .onFocusChanged { isFocused = it.isFocused }
                                                .focusable()
                                                .clickable {
                                                    errorMessage = ""
                                                    when (key) {
                                                        "Clear" -> enteredOtp = ""
                                                        "Delete" -> if (enteredOtp.isNotEmpty()) enteredOtp = enteredOtp.dropLast(1)
                                                        else -> {
                                                            if (enteredOtp.length < 6) {
                                                                enteredOtp += key
                                                            }
                                                            if (enteredOtp.length == 6) {
                                                                if (enteredOtp == simulatedCode) {
                                                                    step = 3
                                                                    errorMessage = ""
                                                                } else {
                                                                    errorMessage = mismatchErrMsg
                                                                }
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
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        Text(
                            text = stringResource(R.string.recovery_step_3_prompt),
                            style = MaterialTheme.typography.bodyMedium.copy(color = AccentGreen),
                            textAlign = TextAlign.Center
                        )

                        // 4-digit PIN representation
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 4) {
                                val filled = i < newPinText.length
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                                )
                            }
                        }

                        if (errorMessage.isNotEmpty()) {
                            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("Clear", "0", "Delete")
                        )

                        val pinLengthErrMsg = stringResource(R.string.recovery_err_pin_length)

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            keys.forEach { rowKeys ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    rowKeys.forEach { key ->
                                        var isFocused by remember { mutableStateOf(false) }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                .onFocusChanged { isFocused = it.isFocused }
                                                .focusable()
                                                .clickable {
                                                    errorMessage = ""
                                                    when (key) {
                                                        "Clear" -> newPinText = ""
                                                        "Delete" -> if (newPinText.isNotEmpty()) newPinText = newPinText.dropLast(1)
                                                        else -> {
                                                            if (newPinText.length < 4) newPinText += key
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
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        var isSaveFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                if (newPinText.length == 4) {
                                    onVerifyAndReset(
                                        enteredOtp,
                                        newPinText,
                                        { onDismiss() },
                                        { err -> errorMessage = err }
                                    )
                                } else {
                                    errorMessage = pinLengthErrMsg
                                }
                            },
                            enabled = newPinText.length == 4,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSaveFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSaveFocused = it.isFocused }
                                .border(
                                    width = if (isSaveFocused) 2.dp else 1.dp,
                                    color = if (isSaveFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text(
                                text = stringResource(R.string.recovery_btn_save),
                                color = if (newPinText.length == 4) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Close / Cancel
                var isCloseFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isCloseFocused = it.isFocused }
                        .border(
                            width = 1.dp,
                            color = if (isCloseFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(stringResource(R.string.btn_close), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }
        }
    }
}

/**
 * TV Fullscreen Lock Screen showing Screen Time limits or Bedtime rest messages.
 */
@Composable
fun FullscreenLockOverlay(
    reason: LockReason,
    onBypassClicked: () -> Unit
) {
    val bgStart = MaterialTheme.colorScheme.surface
    val bgEnd = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(bgStart, bgEnd),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            val icon = if (reason == LockReason.BEDTIME) Icons.Default.Bedtime else Icons.Default.Timer
            val title = if (reason == LockReason.BEDTIME) stringResource(R.string.lock_bedtime_title) else stringResource(R.string.lock_screentime_title)
            val desc = if (reason == LockReason.BEDTIME) {
                stringResource(R.string.lock_bedtime_desc)
            } else {
                stringResource(R.string.lock_screentime_desc)
            }
            val accentColor = if (reason == LockReason.BEDTIME) CinematicVioletGlow else AccentOrange

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(2.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(54.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = desc,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 500.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            var isBypassBtnFocused by remember { mutableStateOf(false) }
            val bypassScale by animateFloatAsState(targetValue = if (isBypassBtnFocused) 1.08f else 1.0f)

            Button(
                onClick = onBypassClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBypassBtnFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .scale(bypassScale)
                    .onFocusChanged { isBypassBtnFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (isBypassBtnFocused) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = null,
                        tint = if (isBypassBtnFocused) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.btn_parental_override),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isBypassBtnFocused) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

/**
 * TV App Options Dialog allowing parents/users to launch, toggle block, view info, or uninstall an app.
 */
@Composable
fun TvAppOptionsDialog(
    appName: String,
    packageName: String,
    icon: Drawable,
    isBlocked: Boolean,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    onToggleBlock: () -> Unit,
    onUninstallApp: () -> Unit,
    onAppInfo: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(440.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with App Icon and Names
                androidx.compose.foundation.Image(
                    bitmap = drawableToImageBitmap(icon),
                    contentDescription = appName,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = packageName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action 1: Open App
                var isOpenFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onOpenApp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOpenFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isOpenFocused = it.isFocused }
                        .border(
                            1.dp,
                            if (isOpenFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (isOpenFocused) Color.White else MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_dialog_open),
                        color = if (isOpenFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action 2: Block / Unblock Toggle
                var isBlockFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onToggleBlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBlockFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isBlockFocused = it.isFocused }
                        .border(
                            1.dp,
                            if (isBlockFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isBlockFocused) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBlocked) stringResource(R.string.app_dialog_unblock) else stringResource(R.string.app_dialog_block),
                        color = if (isBlockFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action 3: App Info / Settings
                var isInfoFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onAppInfo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInfoFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isInfoFocused = it.isFocused }
                        .border(
                            1.dp,
                            if (isInfoFocused) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = if (isInfoFocused) Color.White else MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_dialog_info),
                        color = if (isInfoFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action 4: Uninstall from TV
                var isUninstallFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onUninstallApp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUninstallFocused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isUninstallFocused = it.isFocused }
                        .border(
                            1.dp,
                            if (isUninstallFocused) Color.White else MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = if (isUninstallFocused) Color.White else MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_dialog_uninstall),
                        color = if (isUninstallFocused) Color.White else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action 5: Cancel / Dismiss
                var isCancelFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isCancelFocused = it.isFocused }
                        .border(
                            1.dp,
                            if (isCancelFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = stringResource(R.string.btn_cancel),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
