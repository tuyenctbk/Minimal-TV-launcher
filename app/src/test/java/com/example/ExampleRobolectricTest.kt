package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
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
}


