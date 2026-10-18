package flutter.overlay.window.flutter_overlay_window

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayConstantsTest {

    @Test
    fun testChannelIdentifiers() {
        assertEquals("myCachedEngine", OverlayConstants.CACHED_TAG)
        assertEquals("x-slayer/overlay_channel", OverlayConstants.CHANNEL_TAG)
        assertEquals("x-slayer/overlay", OverlayConstants.OVERLAY_TAG)
        assertEquals("x-slayer/overlay_messenger", OverlayConstants.MESSENGER_TAG)
    }

    @Test
    fun testNotificationConstants() {
        assertEquals("Overlay Channel", OverlayConstants.CHANNEL_ID)
        assertEquals(4579, OverlayConstants.NOTIFICATION_ID)
    }

    @Test
    fun testPositionDefaults() {
        assertEquals(-6, OverlayConstants.DEFAULT_XY)
    }

    @Test
    fun testDismissConstants() {
        assertEquals("enableDismiss", OverlayConstants.ENABLE_DISMISS)
        assertEquals("dismissIcon", OverlayConstants.DISMISS_ICON)
        assertEquals("dismissIconColor", OverlayConstants.DISMISS_ICON_COLOR)
        assertEquals("dismissBackgroundColor", OverlayConstants.DISMISS_BG_COLOR)
    }
}
