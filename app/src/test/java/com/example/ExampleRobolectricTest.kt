package com.example

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Minimal TV Launcher", appName)
  }

  @Test
  fun `verify tv_banner resource exists and loads`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = ContextCompat.getDrawable(context, R.drawable.tv_banner)
    assertNotNull("tv_banner drawable should be non-null", drawable)
    assertTrue("tv_banner intrinsic width > 0", drawable!!.intrinsicWidth > 0)
    assertTrue("tv_banner intrinsic height > 0", drawable.intrinsicHeight > 0)
  }

  @Test
  fun `verify launcher icon resource exists and loads`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    assertNotNull("ic_launcher drawable should be non-null", icon)
  }

  @Test
  fun `generate high quality skia tv banner and icons`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Render 512x512 Icon using Skia VectorDrawables (ic_launcher_background + ic_launcher_foreground)
    val bgDrawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)!!
    val fgDrawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!

    val iconBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    val iconCanvas = Canvas(iconBitmap)
    bgDrawable.setBounds(0, 0, 512, 512)
    bgDrawable.draw(iconCanvas)
    // Scale foreground slightly to fill the 512x512 full-bleed canvas nicely
    iconCanvas.save()
    iconCanvas.scale(1.02f, 1.02f, 256f, 256f)
    fgDrawable.setBounds(0, 0, 512, 512)
    fgDrawable.draw(iconCanvas)
    iconCanvas.restore()

    FileOutputStream(File("/tmp/skia_icon_512.png")).use {
      iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }

    // 2. Render 1280x720 TV Banner using Skia Canvas
    val bannerBitmap = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bannerBitmap)

    // Scale coordinate system from 320x180 -> 1280x720 (4x)
    canvas.save()
    canvas.scale(4f, 4f)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background Linear Gradient
    paint.shader = LinearGradient(
      0f, 0f, 320f, 180f,
      intArrayOf(Color.parseColor("#070B14"), Color.parseColor("#0F172A"), Color.parseColor("#1E1B4B")),
      floatArrayOf(0f, 0.5f, 1f),
      Shader.TileMode.CLAMP
    )
    canvas.drawRect(0f, 0f, 320f, 180f, paint)

    // Ambient Glow Orbs
    paint.shader = RadialGradient(
      90f, 90f, 75f,
      intArrayOf(Color.parseColor("#356366F1"), Color.TRANSPARENT),
      floatArrayOf(0f, 1f),
      Shader.TileMode.CLAMP
    )
    canvas.drawCircle(90f, 90f, 75f, paint)

    paint.shader = RadialGradient(
      260f, 100f, 80f,
      intArrayOf(Color.parseColor("#2538BDF8"), Color.TRANSPARENT),
      floatArrayOf(0f, 1f),
      Shader.TileMode.CLAMP
    )
    canvas.drawCircle(260f, 100f, 80f, paint)
    paint.shader = null

    // TV Stand Pillar & Base on the left
    paint.color = Color.parseColor("#4F46E5")
    canvas.drawRect(84f, 122f, 96f, 142f, paint)

    paint.shader = LinearGradient(
      68f, 142f, 115f, 150f,
      Color.parseColor("#818CF8"), Color.parseColor("#6366F1"),
      Shader.TileMode.CLAMP
    )
    canvas.drawRoundRect(RectF(65f, 142f, 115f, 150f), 3f, 3f, paint)
    paint.shader = null

    // TV Display Outer Frame
    paint.shader = LinearGradient(
      30f, 38f, 150f, 122f,
      Color.parseColor("#6366F1"), Color.parseColor("#312E81"),
      Shader.TileMode.CLAMP
    )
    canvas.drawRoundRect(RectF(30f, 38f, 150f, 122f), 10f, 10f, paint)
    paint.shader = null

    // TV Screen Inner Deep Glass Canvas
    paint.color = Color.parseColor("#090D16")
    canvas.drawRoundRect(RectF(40f, 43f, 140f, 107f), 5f, 5f, paint)

    // Screen Glare Diagonal Stripe
    val glarePath = Path().apply {
      moveTo(50f, 43f)
      lineTo(95f, 43f)
      lineTo(72f, 107f)
      lineTo(50f, 107f)
      close()
    }
    paint.color = Color.parseColor("#15FFFFFF")
    canvas.drawPath(glarePath, paint)

    // Reset alpha to full opaque before drawing shield!
    paint.color = Color.WHITE
    paint.alpha = 255

    // Protective Shield Emblem inside TV Screen (Bezier curves)
    val shieldOuter = Path().apply {
      moveTo(90f, 52f)
      cubicTo(102f, 52f, 108f, 57f, 108f, 57f)
      cubicTo(108f, 77f, 97f, 89f, 90f, 93f)
      cubicTo(83f, 89f, 72f, 77f, 72f, 57f)
      cubicTo(72f, 57f, 78f, 52f, 90f, 52f)
      close()
    }
    paint.shader = LinearGradient(
      72f, 52f, 108f, 93f,
      Color.parseColor("#38BDF8"), Color.parseColor("#6366F1"),
      Shader.TileMode.CLAMP
    )
    canvas.drawPath(shieldOuter, paint)

    val shieldInner = Path().apply {
      moveTo(90f, 56f)
      cubicTo(99f, 56f, 104f, 60f, 104f, 60f)
      cubicTo(104f, 74f, 95f, 84f, 90f, 87f)
      cubicTo(85f, 84f, 76f, 74f, 76f, 60f)
      cubicTo(76f, 60f, 81f, 56f, 90f, 56f)
      close()
    }
    paint.shader = LinearGradient(
      76f, 56f, 104f, 87f,
      Color.parseColor("#0EA5E9"), Color.parseColor("#4F46E5"),
      Shader.TileMode.CLAMP
    )
    canvas.drawPath(shieldInner, paint)
    paint.shader = null

    // Play Arrow inside Shield
    val playPath = Path().apply {
      moveTo(87f, 63f)
      lineTo(97f, 69f)
      lineTo(87f, 75f)
      close()
    }
    paint.color = Color.WHITE
    canvas.drawPath(playPath, paint)

    // Power Indicator LED
    paint.color = Color.parseColor("#10B981")
    canvas.drawCircle(90f, 114.5f, 1.8f, paint)

    // RIGHT SIDE BRANDING:
    // 1. Pill Badge: PARENTAL CONTROL
    paint.color = Color.parseColor("#286366F1")
    canvas.drawRoundRect(RectF(164f, 36f, 298f, 54f), 9f, 9f, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 0.8f
    paint.color = Color.parseColor("#818CF8")
    canvas.drawRoundRect(RectF(164f, 36f, 298f, 54f), 9f, 9f, paint)
    paint.style = Paint.Style.FILL

    // Small Shield in Badge
    val smallShield = Path().apply {
      moveTo(175f, 40f)
      cubicTo(177.5f, 40f, 179.5f, 41f, 179.5f, 41f)
      cubicTo(179.5f, 46.5f, 176.8f, 49.2f, 175f, 50.5f)
      cubicTo(173.2f, 49.2f, 170.5f, 46.5f, 170.5f, 41f)
      cubicTo(170.5f, 41f, 172.5f, 40f, 175f, 40f)
      close()
    }
    paint.color = Color.parseColor("#38BDF8")
    canvas.drawPath(smallShield, paint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    textPaint.color = Color.parseColor("#C7D2FE")
    textPaint.textSize = 6.8f
    textPaint.letterSpacing = 0.08f
    canvas.drawText("PARENTAL CONTROL", 184f, 47.5f, textPaint)

    // 2. Main Title Line 1: MINIMAL + [TV]
    textPaint.color = Color.WHITE
    textPaint.textSize = 21f
    textPaint.letterSpacing = 0.03f
    canvas.drawText("MINIMAL", 164f, 80f, textPaint)

    val minimalWidth = textPaint.measureText("MINIMAL")
    val tvPillLeft = 164f + minimalWidth + 5f
    val tvPillRect = RectF(tvPillLeft, 61.5f, tvPillLeft + 34f, 83.5f)
    paint.color = Color.parseColor("#0EA5E9")
    canvas.drawRoundRect(tvPillRect, 4.5f, 4.5f, paint)

    textPaint.color = Color.WHITE
    textPaint.textSize = 15.5f
    textPaint.letterSpacing = 0.04f
    val tvTextWidth = textPaint.measureText("TV")
    canvas.drawText("TV", tvPillRect.centerX() - tvTextWidth / 2f, 78f, textPaint)

    // 3. Main Title Line 2: LAUNCHER
    textPaint.color = Color.parseColor("#818CF8")
    textPaint.textSize = 19.5f
    textPaint.letterSpacing = 0.09f
    canvas.drawText("LAUNCHER", 164f, 104f, textPaint)

    // 4. Subtitle Tagline
    textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    textPaint.color = Color.parseColor("#94A3B8")
    textPaint.textSize = 6.8f
    textPaint.letterSpacing = 0.05f
    canvas.drawText("Kids & Family Safe Home Screen", 165f, 117f, textPaint)

    // 5. Feature Badges: App Guard, Screen Time, Bedtime
    val badges = listOf(
      Triple("App Guard", "#10B981", 164f to 208f),
      Triple("Screen Time", "#F59E0B", 212f to 260f),
      Triple("Bedtime", "#6366F1", 264f to 304f)
    )
    textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    textPaint.textSize = 5.2f
    textPaint.letterSpacing = 0.02f

    for ((label, dotColor, range) in badges) {
      val (left, right) = range
      paint.color = Color.parseColor("#1E293B")
      canvas.drawRoundRect(RectF(left, 125f, right, 141f), 4f, 4f, paint)
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 0.5f
      paint.color = Color.parseColor("#334155")
      canvas.drawRoundRect(RectF(left, 125f, right, 141f), 4f, 4f, paint)
      paint.style = Paint.Style.FILL

      paint.color = Color.parseColor(dotColor)
      canvas.drawCircle(left + 5.5f, 133f, 1.8f, paint)

      textPaint.color = Color.parseColor("#E2E8F0")
      canvas.drawText(label, left + 9.5f, 134.8f, textPaint)
    }

    // Bottom Decorative Ambient Line
    paint.shader = LinearGradient(
      0f, 178f, 320f, 180f,
      intArrayOf(Color.TRANSPARENT, Color.parseColor("#6366F1"), Color.TRANSPARENT),
      floatArrayOf(0f, 0.5f, 1f),
      Shader.TileMode.CLAMP
    )
    canvas.drawRect(0f, 178f, 320f, 180f, paint)
    paint.shader = null

    canvas.restore()

    FileOutputStream(File("/tmp/skia_banner_1280x720.png")).use {
      bannerBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
  }
}



