package flutter.overlay.window.flutter_overlay_window

import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import io.flutter.plugin.common.BasicMessageChannel

class WindowSetup private constructor() {

    var height: Int = WindowManager.LayoutParams.MATCH_PARENT
    var width: Int = WindowManager.LayoutParams.MATCH_PARENT
    var flag: Int = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    var gravity: Int = Gravity.CENTER
    var messenger: BasicMessageChannel<Any?>? = null
    var overlayTitle: String = "Overlay is activated"
    var overlayContent: String = "Tap to edit settings or disable"
    var positionGravity: String = "none"
    var notificationVisibility: Int = NotificationCompat.VISIBILITY_PRIVATE
    var enableDrag: Boolean = false
    var notificationIcon: String? = null
    var enableDismiss: Boolean = false
    var dismissIconColor: Int = 0xFFFFFFFF.toInt()
    var dismissBackgroundColor: Int = 0xCC333333.toInt()
    var dismissIcon: String? = null

    companion object {
        @JvmStatic
        val instance: WindowSetup by lazy { WindowSetup() }
    }

    fun setNotificationVisibility(name: String?) {
        if (name == null) return
        when {
            name.equals("visibilityPublic", ignoreCase = true) -> {
                notificationVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            name.equals("visibilitySecret", ignoreCase = true) -> {
                notificationVisibility = NotificationCompat.VISIBILITY_SECRET
            }
            name.equals("visibilityPrivate", ignoreCase = true) -> {
                notificationVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }
        }
    }

    fun setFlag(name: String?) {
        if (name == null) return
        when {
            name.equals("flagNotFocusable", ignoreCase = true) || name.equals("defaultFlag", ignoreCase = true) -> {
                flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            name.equals("flagNotTouchable", ignoreCase = true) || name.equals("clickThrough", ignoreCase = true) -> {
                flag = (WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            }
            name.equals("flagNotTouchModal", ignoreCase = true) || name.equals("focusPointer", ignoreCase = true) -> {
                flag = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }
        }
    }

    fun setGravityFromAlignment(alignment: String?) {
        if (alignment == null) return
        gravity = when {
            alignment.equals("topLeft", ignoreCase = true) -> Gravity.TOP or Gravity.LEFT
            alignment.equals("topCenter", ignoreCase = true) -> Gravity.TOP
            alignment.equals("topRight", ignoreCase = true) -> Gravity.TOP or Gravity.RIGHT
            alignment.equals("centerLeft", ignoreCase = true) -> Gravity.CENTER or Gravity.LEFT
            alignment.equals("center", ignoreCase = true) -> Gravity.CENTER
            alignment.equals("centerRight", ignoreCase = true) -> Gravity.CENTER or Gravity.RIGHT
            alignment.equals("bottomLeft", ignoreCase = true) -> Gravity.BOTTOM or Gravity.LEFT
            alignment.equals("bottomCenter", ignoreCase = true) -> Gravity.BOTTOM
            alignment.equals("bottomRight", ignoreCase = true) -> Gravity.BOTTOM or Gravity.RIGHT
            else -> Gravity.CENTER
        }
    }
}
