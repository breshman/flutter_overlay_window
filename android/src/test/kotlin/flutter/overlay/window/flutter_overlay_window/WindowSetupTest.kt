package flutter.overlay.window.flutter_overlay_window

import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WindowSetupTest {

    private lateinit var windowSetup: WindowSetup

    @Before
    fun setUp() {
        windowSetup = WindowSetup.instance
    }

    @Test
    fun testSingletonInstanceNotNull() {
        assertNotNull(windowSetup)
        assertEquals(windowSetup, WindowSetup.instance)
    }

    @Test
    fun testDefaultDismissProperties() {
        assertFalse(windowSetup.enableDismiss)
        assertEquals(0xFFFFFFFF.toInt(), windowSetup.dismissIconColor)
        assertEquals(0xCC333333.toInt(), windowSetup.dismissBackgroundColor)
        assertEquals(null, windowSetup.dismissIcon)
    }

    @Test
    fun testSetDismissProperties() {
        windowSetup.enableDismiss = true
        windowSetup.dismissIconColor = 0xFFE53935.toInt()
        windowSetup.dismissBackgroundColor = 0xAA000000.toInt()
        windowSetup.dismissIcon = "ic_custom_dismiss"

        assertTrue(windowSetup.enableDismiss)
        assertEquals(0xFFE53935.toInt(), windowSetup.dismissIconColor)
        assertEquals(0xAA000000.toInt(), windowSetup.dismissBackgroundColor)
        assertEquals("ic_custom_dismiss", windowSetup.dismissIcon)

        // Reset
        windowSetup.enableDismiss = false
        windowSetup.dismissIcon = null
    }

    @Test
    fun testSetGravityFromAlignment() {
        windowSetup.setGravityFromAlignment("topLeft")
        assertEquals(Gravity.TOP or Gravity.LEFT, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("topCenter")
        assertEquals(Gravity.TOP, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("topRight")
        assertEquals(Gravity.TOP or Gravity.RIGHT, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("centerLeft")
        assertEquals(Gravity.CENTER or Gravity.LEFT, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("center")
        assertEquals(Gravity.CENTER, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("centerRight")
        assertEquals(Gravity.CENTER or Gravity.RIGHT, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("bottomLeft")
        assertEquals(Gravity.BOTTOM or Gravity.LEFT, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("bottomCenter")
        assertEquals(Gravity.BOTTOM, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("bottomRight")
        assertEquals(Gravity.BOTTOM or Gravity.RIGHT, windowSetup.gravity)

        windowSetup.setGravityFromAlignment("unknown")
        assertEquals(Gravity.CENTER, windowSetup.gravity)
    }

    @Test
    fun testSetFlag() {
        windowSetup.setFlag("defaultFlag")
        assertEquals(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, windowSetup.flag)

        windowSetup.setFlag("flagNotFocusable")
        assertEquals(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, windowSetup.flag)

        windowSetup.setFlag("focusPointer")
        assertEquals(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, windowSetup.flag)

        windowSetup.setFlag("flagNotTouchModal")
        assertEquals(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, windowSetup.flag)

        windowSetup.setFlag("clickThrough")
        val expectedClickThrough = (WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        assertEquals(expectedClickThrough, windowSetup.flag)
    }

    @Test
    fun testSetNotificationVisibility() {
        windowSetup.setNotificationVisibility("visibilityPublic")
        assertEquals(NotificationCompat.VISIBILITY_PUBLIC, windowSetup.notificationVisibility)

        windowSetup.setNotificationVisibility("visibilitySecret")
        assertEquals(NotificationCompat.VISIBILITY_SECRET, windowSetup.notificationVisibility)

        windowSetup.setNotificationVisibility("visibilityPrivate")
        assertEquals(NotificationCompat.VISIBILITY_PRIVATE, windowSetup.notificationVisibility)
    }

    @Test
    fun testDimensionsAndTitles() {
        windowSetup.width = 320
        windowSetup.height = 480
        windowSetup.overlayTitle = "Unit Test Title"
        windowSetup.overlayContent = "Unit Test Content"
        windowSetup.enableDrag = true
        windowSetup.positionGravity = "auto"

        assertEquals(320, windowSetup.width)
        assertEquals(480, windowSetup.height)
        assertEquals("Unit Test Title", windowSetup.overlayTitle)
        assertEquals("Unit Test Content", windowSetup.overlayContent)
        assertTrue(windowSetup.enableDrag)
        assertEquals("auto", windowSetup.positionGravity)
    }
}
