package flutter.overlay.window.flutter_overlay_window

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.flutter.FlutterInjector
import io.flutter.embedding.android.FlutterTextureView
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class OverlayService : Service(), View.OnTouchListener {

    private val DEFAULT_NAV_BAR_HEIGHT_DP = 48
    private val DEFAULT_STATUS_BAR_HEIGHT_DP = 25
    private var mScreenHeight = -1
    private var mDensity = -1f
    private var mStatusBarHeight: Int? = -1
    private var mNavigationBarHeight: Int? = -1
    private lateinit var mResources: Resources

    private var windowManager: WindowManager? = null
    private var flutterView: FlutterView? = null
    private var flutterChannel: MethodChannel? = null
    private var overlayMessageChannel: BasicMessageChannel<Any?>? = null
    private var dismissView: FrameLayout? = null
    private var dismissTargetView: ImageView? = null
    private val DISMISS_SIZE_DP = 56
    private val DISMISS_WINDOW_SIZE_DP = 120
    private val DISMISS_BOTTOM_MARGIN_DP = 48
    private val windowSetup = WindowSetup.instance

    private val clickableFlag = (WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

    private val mAnimationHandler = Handler(Looper.getMainLooper())
    private var lastX = 0f
    private var lastY = 0f
    private var lastYPosition = 0
    private var dragging = false
    private var dragSlopSquared = 576
    private var isCurrentlyHoveringDismiss = false

    private val szWindow = Point()
    private var mTrayAnimationTimer: Timer? = null
    private var mTrayTimerTask: TrayAnimationTimerTask? = null

    companion object {
        const val INTENT_EXTRA_IS_CLOSE_WINDOW = "IsCloseWindow"
        private const val MAXIMUM_OPACITY_ALLOWED_FOR_S_AND_HIGHER = 0.8f

        @JvmStatic
        var isRunning: Boolean = false
            private set

        @SuppressLint("StaticFieldLeak")
        private var instance: OverlayService? = null

        @JvmStatic
        fun getCurrentPosition(): Map<String, Double>? {
            val inst = instance ?: return null
            val view = inst.flutterView ?: return null
            val params = view.layoutParams as? WindowManager.LayoutParams ?: return null
            return mapOf(
                "x" to inst.pxToDp(params.x),
                "y" to inst.pxToDp(params.y)
            )
        }

        @JvmStatic
        fun moveOverlay(x: Int, y: Int): Boolean {
            val inst = instance ?: return false
            val view = inst.flutterView ?: return false
            val wm = inst.windowManager ?: return false
            val params = view.layoutParams as? WindowManager.LayoutParams ?: return false

            params.x = if (x == -1999 || x == -1) -1 else inst.dpToPx(x)
            params.y = inst.dpToPx(y)
            wm.updateViewLayout(view, params)
            return true
        }

        @JvmStatic
        fun updateNotification(): Boolean {
            val inst = instance ?: return false
            val manager = inst.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return false
            manager.notify(OverlayConstants.NOTIFICATION_ID, inst.buildNotification())
            return true
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        mResources = applicationContext.resources
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        dragSlopSquared = slop * slop

        var flutterEngine = FlutterEngineCache.getInstance().get(OverlayConstants.CACHED_TAG)
        if (flutterEngine == null) {
            Log.e("OverlayService", "Flutter engine not found, creating new flutter engine")
            val engineGroup = FlutterEngineGroup(this)
            val entryPoint = DartExecutor.DartEntrypoint(
                FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                "overlayMain"
            )
            flutterEngine = engineGroup.createAndRunEngine(this, entryPoint)
            FlutterEngineCache.getInstance().put(OverlayConstants.CACHED_TAG, flutterEngine)
        }

        if (flutterEngine != null) {
            flutterChannel = MethodChannel(flutterEngine.dartExecutor, OverlayConstants.OVERLAY_TAG)
            overlayMessageChannel = BasicMessageChannel(
                flutterEngine.dartExecutor,
                OverlayConstants.MESSENGER_TAG,
                StandardMessageCodec.INSTANCE
            )
        }

        createNotificationChannel()

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            }
            ServiceCompat.startForeground(
                this,
                OverlayConstants.NOTIFICATION_ID,
                notification,
                foregroundServiceType
            )
        } else {
            startForeground(OverlayConstants.NOTIFICATION_ID, notification)
        }
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY

        val isCloseWindow = intent.getBooleanExtra(INTENT_EXTRA_IS_CLOSE_WINDOW, false)
        if (isCloseWindow) {
            cleanupAndStopSelf()
            return START_STICKY
        }

        // If an overlay view is already running, clean up the previous view smoothly
        // without terminating the Foreground Service lifecycle.
        if (flutterView != null || isRunning) {
            removeCurrentOverlayView()
        }

        // Extract parameters directly from intent extras for self-contained configuration
        val startX = intent.getIntExtra("startX", OverlayConstants.DEFAULT_XY)
        val startY = intent.getIntExtra("startY", OverlayConstants.DEFAULT_XY)
        val width = intent.getIntExtra("width", windowSetup.width)
        val height = intent.getIntExtra("height", windowSetup.height)
        val alignment = intent.getStringExtra("alignment")
        val flag = intent.getStringExtra("flag")
        val overlayTitle = intent.getStringExtra("overlayTitle")
        val overlayContent = intent.getStringExtra("overlayContent")
        val enableDrag = intent.getBooleanExtra("enableDrag", windowSetup.enableDrag)
        val enableDismiss = intent.getBooleanExtra(OverlayConstants.ENABLE_DISMISS, windowSetup.enableDismiss)
        val dismissIcon = intent.getStringExtra(OverlayConstants.DISMISS_ICON) ?: windowSetup.dismissIcon
        val dismissIconColor = if (intent.hasExtra(OverlayConstants.DISMISS_ICON_COLOR)) {
            intent.getIntExtra(OverlayConstants.DISMISS_ICON_COLOR, windowSetup.dismissIconColor)
        } else {
            windowSetup.dismissIconColor
        }
        val dismissBackgroundColor = if (intent.hasExtra(OverlayConstants.DISMISS_BG_COLOR)) {
            intent.getIntExtra(OverlayConstants.DISMISS_BG_COLOR, windowSetup.dismissBackgroundColor)
        } else {
            windowSetup.dismissBackgroundColor
        }
        val positionGravity = intent.getStringExtra("positionGravity")
        val notificationVisibility = intent.getStringExtra("notificationVisibility")
        val notificationIcon = intent.getStringExtra("notificationIcon")

        windowSetup.width = width
        windowSetup.height = height
        windowSetup.enableDrag = enableDrag
        windowSetup.enableDismiss = enableDismiss
        windowSetup.dismissIcon = dismissIcon
        windowSetup.dismissIconColor = dismissIconColor
        windowSetup.dismissBackgroundColor = dismissBackgroundColor
        if (alignment != null) windowSetup.setGravityFromAlignment(alignment)
        if (flag != null) windowSetup.setFlag(flag)
        if (overlayTitle != null) windowSetup.overlayTitle = overlayTitle
        if (overlayContent != null) windowSetup.overlayContent = overlayContent
        if (positionGravity != null) windowSetup.positionGravity = positionGravity
        if (notificationVisibility != null) windowSetup.setNotificationVisibility(notificationVisibility)
        if (notificationIcon != null) windowSetup.notificationIcon = notificationIcon

        isRunning = true
        Log.d("onStartCommand", "Service started")

        val engine = FlutterEngineCache.getInstance().get(OverlayConstants.CACHED_TAG)
        engine?.lifecycleChannel?.appIsResumed()

        val textureView = FlutterTextureView(applicationContext)
        flutterView = FlutterView(applicationContext, textureView).apply {
            if (engine != null) {
                attachToFlutterEngine(engine)
            }
            fitsSystemWindows = true
            isFocusable = true
            isFocusableInTouchMode = true
            setBackgroundColor(Color.TRANSPARENT)
        }

        flutterChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "updateFlag" -> {
                    val newFlag: String? = call.argument("flag")
                    updateOverlayFlag(result, newFlag)
                }
                "updateOverlayPosition" -> {
                    val x: Int = call.argument("x") ?: 0
                    val y: Int = call.argument("y") ?: 0
                    moveOverlay(x, y, result)
                }
                "resizeOverlay" -> {
                    val newWidth: Int = call.argument("width") ?: -1
                    val newHeight: Int = call.argument("height") ?: -1
                    val newEnableDrag: Boolean = call.argument("enableDrag") ?: false
                    resizeOverlay(newWidth, newHeight, newEnableDrag, result)
                }
            }
        }

        overlayMessageChannel?.setMessageHandler { message, _ ->
            windowSetup.messenger?.send(message)
        }

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm
        updateScreenDimensions()

        val dx = if (startX == OverlayConstants.DEFAULT_XY) 0 else startX
        val dy = if (startY == OverlayConstants.DEFAULT_XY) -statusBarHeightPx() else startY

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            when (windowSetup.width) {
                -1999, -1 -> WindowManager.LayoutParams.MATCH_PARENT
                -2 -> WindowManager.LayoutParams.WRAP_CONTENT
                else -> dpToPx(windowSetup.width)
            },
            when (windowSetup.height) {
                -1999 -> screenHeight()
                -1 -> WindowManager.LayoutParams.MATCH_PARENT
                -2 -> WindowManager.LayoutParams.WRAP_CONTENT
                else -> dpToPx(windowSetup.height)
            },
            0,
            -statusBarHeightPx(),
            layoutType,
            windowSetup.flag or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowSetup.flag == clickableFlag) {
            params.alpha = MAXIMUM_OPACITY_ALLOWED_FOR_S_AND_HIGHER
        }
        params.gravity = windowSetup.gravity
        flutterView?.setOnTouchListener(this)
        wm.addView(flutterView, params)
        moveOverlay(dx, dy, null)

        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        removeDismissView()
        mScreenHeight = -1
        mStatusBarHeight = -1
        mNavigationBarHeight = -1
        updateScreenDimensions()

        val view = flutterView ?: return
        val wm = windowManager ?: return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return
        constrainToSafeBoundaries(params, view.width, view.height)
        wm.updateViewLayout(view, params)
    }

    private fun updateScreenDimensions() {
        val wm = windowManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val bounds = windowMetrics.bounds
            szWindow.set(bounds.width(), bounds.height())
        } else {
            @Suppress("DEPRECATION")
            val display = wm.defaultDisplay
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getRealMetrics(dm)
            szWindow.set(dm.widthPixels, dm.heightPixels)
        }
    }

    private fun removeCurrentOverlayView() {
        mTrayAnimationTimer?.cancel()
        mTrayAnimationTimer = null
        mTrayTimerTask?.cancel()
        mTrayTimerTask = null
        removeDismissView(immediate = true)
        if (windowManager != null && flutterView != null) {
            try {
                windowManager?.removeView(flutterView)
            } catch (e: Exception) {
                Log.e("OverlayService", "Error removing flutterView", e)
            }
        }
        val engine = FlutterEngineCache.getInstance().get(OverlayConstants.CACHED_TAG)
        if (engine != null && flutterView != null) {
            try {
                flutterView?.detachFromFlutterEngine()
            } catch (e: Exception) {
                Log.e("OverlayService", "Error detaching Flutter engine", e)
            }
        }
        flutterView = null
    }

    private fun cleanupAndStopSelf() {
        removeCurrentOverlayView()
        windowManager = null
        isRunning = false
        instance = null
        stopSelf()
    }

    override fun onDestroy() {
        Log.d("OverLay", "Destroying the overlay window service")
        removeCurrentOverlayView()
        windowManager = null
        isRunning = false
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(OverlayConstants.NOTIFICATION_ID)
        instance = null
        super.onDestroy()
    }

    private fun screenHeight(): Int {
        if (mScreenHeight == -1) {
            updateScreenDimensions()
            mScreenHeight = if (inPortrait()) {
                szWindow.y + statusBarHeightPx() + navigationBarHeightPx()
            } else {
                szWindow.y + statusBarHeightPx()
            }
        }
        return mScreenHeight
    }

    private fun statusBarHeightPx(): Int {
        if (mStatusBarHeight == null || mStatusBarHeight == -1) {
            val statusBarHeightId = mResources.getIdentifier("status_bar_height", "dimen", "android")
            mStatusBarHeight = if (statusBarHeightId > 0) {
                mResources.getDimensionPixelSize(statusBarHeightId)
            } else {
                dpToPx(DEFAULT_STATUS_BAR_HEIGHT_DP)
            }
        }
        return mStatusBarHeight ?: dpToPx(DEFAULT_STATUS_BAR_HEIGHT_DP)
    }

    private fun navigationBarHeightPx(): Int {
        if (mNavigationBarHeight == null || mNavigationBarHeight == -1) {
            val navBarHeightId = mResources.getIdentifier("navigation_bar_height", "dimen", "android")
            mNavigationBarHeight = if (navBarHeightId > 0) {
                mResources.getDimensionPixelSize(navBarHeightId)
            } else {
                dpToPx(DEFAULT_NAV_BAR_HEIGHT_DP)
            }
        }
        return mNavigationBarHeight ?: dpToPx(DEFAULT_NAV_BAR_HEIGHT_DP)
    }

    private fun getSafeBoundaries(): IntArray {
        val screenWidth = szWindow.x
        val screenHeight = szWindow.y
        val statusBarHeight = statusBarHeightPx()
        val navigationBarHeight = if (inPortrait()) navigationBarHeightPx() else 0

        val leftBound = 0
        val rightBound = screenWidth
        val topBound = statusBarHeight
        val bottomBound = screenHeight - navigationBarHeight

        return intArrayOf(leftBound, topBound, rightBound, bottomBound)
    }

    private fun applySoftBoundaryConstraints(params: WindowManager.LayoutParams, overlayWidth: Int, overlayHeight: Int) {
        val bounds = getSafeBoundaries()
        val leftBound = bounds[0]
        val topBound = bounds[1]
        val rightBound = bounds[2]
        val bottomBound = bounds[3]

        val minVisibleWidth = max(overlayWidth / 5, 20)
        val minVisibleHeight = max(overlayHeight / 5, 20)

        if ((windowSetup.gravity and Gravity.RIGHT) == Gravity.RIGHT) {
            val maxLeftOffset = rightBound - minVisibleWidth
            params.x = max(minVisibleWidth - overlayWidth, min(params.x, maxLeftOffset))
        } else if ((windowSetup.gravity and Gravity.LEFT) == Gravity.LEFT) {
            val maxRightOffset = rightBound - minVisibleWidth
            params.x = max(minVisibleWidth - overlayWidth, min(params.x, maxRightOffset))
        } else {
            val maxLeftOffset = leftBound - (szWindow.x / 2) - (overlayWidth / 2) + minVisibleWidth
            val maxRightOffset = rightBound - (szWindow.x / 2) + (overlayWidth / 2) - minVisibleWidth
            params.x = max(maxLeftOffset, min(params.x, maxRightOffset))
        }

        if ((windowSetup.gravity and Gravity.BOTTOM) == Gravity.BOTTOM) {
            val maxTopOffset = bottomBound - minVisibleHeight
            params.y = max(minVisibleHeight - overlayHeight, min(params.y, maxTopOffset))
        } else if ((windowSetup.gravity and Gravity.TOP) == Gravity.TOP) {
            val maxBottomOffset = bottomBound - minVisibleHeight
            params.y = max(minVisibleHeight - overlayHeight, min(params.y, maxBottomOffset))
        } else {
            val maxTopOffset = topBound - (szWindow.y / 2) - (overlayHeight / 2) + minVisibleHeight
            val maxBottomOffset = bottomBound - (szWindow.y / 2) + (overlayHeight / 2) - minVisibleHeight
            params.y = max(maxTopOffset, min(params.y, maxBottomOffset))
        }
    }

    private fun constrainToSafeBoundaries(params: WindowManager.LayoutParams, overlayWidth: Int, overlayHeight: Int) {
        val bounds = getSafeBoundaries()
        val leftBound = bounds[0]
        val topBound = bounds[1]
        val rightBound = bounds[2]
        val bottomBound = bounds[3]

        if ((windowSetup.gravity and Gravity.RIGHT) == Gravity.RIGHT) {
            params.x = max(0, min(params.x, rightBound - overlayWidth))
        } else if ((windowSetup.gravity and Gravity.LEFT) == Gravity.LEFT) {
            params.x = max(leftBound, min(params.x, rightBound - overlayWidth))
        } else {
            val minX = leftBound - (szWindow.x / 2) + (overlayWidth / 2)
            val maxX = rightBound - (szWindow.x / 2) - (overlayWidth / 2)
            params.x = max(minX, min(params.x, maxX))
        }

        if ((windowSetup.gravity and Gravity.BOTTOM) == Gravity.BOTTOM) {
            params.y = max(0, min(params.y, bottomBound - overlayHeight))
        } else if ((windowSetup.gravity and Gravity.TOP) == Gravity.TOP) {
            params.y = max(topBound, min(params.y, bottomBound - overlayHeight))
        } else {
            val minY = topBound - (szWindow.y / 2) + (overlayHeight / 2)
            val maxY = bottomBound - (szWindow.y / 2) - (overlayHeight / 2)
            params.y = max(minY, min(params.y, maxY))
        }
    }

    private fun updateOverlayFlag(result: MethodChannel.Result, flag: String?) {
        val wm = windowManager
        val view = flutterView
        if (wm != null && view != null) {
            windowSetup.setFlag(flag)
            val params = view.layoutParams as WindowManager.LayoutParams
            params.flags = (windowSetup.flag
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowSetup.flag == clickableFlag) {
                params.alpha = MAXIMUM_OPACITY_ALLOWED_FOR_S_AND_HIGHER
            } else {
                params.alpha = 1f
            }
            wm.updateViewLayout(view, params)
            result.success(true)
        } else {
            result.success(false)
        }
    }

    private fun resizeOverlay(width: Int, height: Int, enableDrag: Boolean, result: MethodChannel.Result) {
        val wm = windowManager
        val view = flutterView
        if (wm != null && view != null) {
            val params = view.layoutParams as WindowManager.LayoutParams
            params.width = if (width == -1999 || width == -1) -1 else dpToPx(width)
            params.height = if (height != 1999 && height != -1) dpToPx(height) else height
            windowSetup.enableDrag = enableDrag
            wm.updateViewLayout(view, params)
            result.success(true)
        } else {
            result.success(false)
        }
    }

    private fun moveOverlay(x: Int, y: Int, result: MethodChannel.Result?) {
        val wm = windowManager
        val view = flutterView
        if (wm != null && view != null) {
            val params = view.layoutParams as WindowManager.LayoutParams
            params.x = if (x == -1999 || x == -1) -1 else dpToPx(x)
            params.y = dpToPx(y)
            wm.updateViewLayout(view, params)
            result?.success(true)
        } else {
            result?.success(false)
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, FlutterOverlayWindowPlugin::class.java)
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingFlags)

        return NotificationCompat.Builder(this, OverlayConstants.CHANNEL_ID)
            .setContentTitle(windowSetup.overlayTitle)
            .setContentText(windowSetup.overlayContent)
            .setSmallIcon(resolveNotificationIcon())
            .setContentIntent(pendingIntent)
            .setVisibility(windowSetup.notificationVisibility)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun resolveNotificationIcon(): Int {
        val custom = windowSetup.notificationIcon
        if (!custom.isNullOrEmpty()) {
            var id = getDrawableResourceId("drawable", custom)
            if (id == 0) {
                id = getDrawableResourceId("mipmap", custom)
            }
            if (id != 0) {
                return id
            }
            Log.w("OverlayService", "Notification icon '$custom' not found, using fallback")
        }
        val id = getDrawableResourceId("mipmap", "ic_launcher")
        return if (id != 0) id else R.drawable.notification_icon
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                OverlayConstants.CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun getDrawableResourceId(resType: String, name: String): Int {
        return applicationContext.resources.getIdentifier(name, resType, applicationContext.packageName)
    }

    private fun dpToPx(dp: Int): Int {
        if (mDensity == -1f) {
            mDensity = mResources.displayMetrics.density
        }
        return (dp * mDensity).roundToInt()
    }

    private fun pxToDp(px: Int): Double {
        if (mDensity == -1f) {
            mDensity = mResources.displayMetrics.density
        }
        return px.toDouble() / mDensity
    }

    private fun inPortrait(): Boolean {
        return mResources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun createDismissView() {
        if (!windowSetup.enableDismiss || dismissView != null || windowManager == null) return
        val targetSizePx = dpToPx(DISMISS_SIZE_DP)
        val windowSizePx = dpToPx(DISMISS_WINDOW_SIZE_DP)
        val centerOffsetFromBottom = dpToPx(DISMISS_BOTTOM_MARGIN_DP + DISMISS_SIZE_DP / 2)

        val container = FrameLayout(this)
        val iv = ImageView(this)
        val customIcon = windowSetup.dismissIcon
        var resolved: Drawable? = null
        if (!customIcon.isNullOrEmpty()) {
            var id = getDrawableResourceId("drawable", customIcon)
            if (id == 0) {
                id = getDrawableResourceId("mipmap", customIcon)
            }
            if (id != 0) {
                try {
                    resolved = ContextCompat.getDrawable(this, id)
                } catch (e: Exception) {
                    Log.w("OverlayService", "Failed to resolve custom dismiss icon: $customIcon", e)
                }
            }
        }
        if (resolved != null) {
            iv.setImageDrawable(resolved)
        } else {
            iv.setImageDrawable(
                DismissDrawable(
                    size = targetSizePx,
                    backgroundColor = windowSetup.dismissBackgroundColor,
                    iconColor = windowSetup.dismissIconColor
                )
            )
        }

        val lp = FrameLayout.LayoutParams(targetSizePx, targetSizePx).apply {
            gravity = Gravity.CENTER
        }
        container.addView(iv, lp)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val dismissParams = WindowManager.LayoutParams(
            windowSizePx,
            windowSizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = max(0, centerOffsetFromBottom - (windowSizePx / 2))
        }

        iv.scaleX = 0f
        iv.scaleY = 0f
        iv.alpha = 0f
        try {
            windowManager?.addView(container, dismissParams)
            dismissView = container
            dismissTargetView = iv
            iv.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(0.85f)
                .setDuration(220)
                .setInterpolator(OvershootInterpolator(1.4f))
                .start()
        } catch (e: Exception) {
            Log.e("OverlayService", "Error displaying dismiss view", e)
        }
    }

    private fun updateDismissProximity(event: MotionEvent) {
        val target = dismissTargetView ?: return
        val sizePx = dpToPx(DISMISS_SIZE_DP)
        val centerOffsetFromBottom = dpToPx(DISMISS_BOTTOM_MARGIN_DP + DISMISS_SIZE_DP / 2)
        val centerX = szWindow.x / 2f
        val centerY = szWindow.y.toFloat() - centerOffsetFromBottom
        val dx = event.rawX - centerX
        val dy = event.rawY - centerY
        val distance = sqrt(dx * dx + dy * dy)
        val dismissRadiusPx = sizePx * 1.15f
        val proximityThresholdPx = dpToPx(240).toFloat()

        val isOver = distance <= dismissRadiusPx

        if (isOver) {
            if (!isCurrentlyHoveringDismiss) {
                isCurrentlyHoveringDismiss = true
                target.animate().cancel()
                target.animate()
                    .scaleX(1.45f)
                    .scaleY(1.45f)
                    .alpha(1.0f)
                    .setDuration(140)
                    .setInterpolator(OvershootInterpolator(1.3f))
                    .start()
                (target.drawable as? DismissDrawable)?.isHovered = true
                try {
                    target.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                } catch (_: Exception) {}
            }
        } else {
            if (isCurrentlyHoveringDismiss) {
                isCurrentlyHoveringDismiss = false
                (target.drawable as? DismissDrawable)?.isHovered = false
                target.animate().cancel()
            }
            // Dynamic proximity zoom effect: scales smoothly from 1.0f up to 1.30f as overlay approaches
            val progress = ((proximityThresholdPx - distance) / (proximityThresholdPx - dismissRadiusPx)).coerceIn(0f, 1f)
            val currentScale = 1.0f + 0.30f * progress
            val currentAlpha = 0.85f + 0.15f * progress
            target.scaleX = currentScale
            target.scaleY = currentScale
            target.alpha = currentAlpha
        }
    }

    private fun removeDismissView(immediate: Boolean = false) {
        val container = dismissView ?: return
        val target = dismissTargetView
        dismissView = null
        dismissTargetView = null
        isCurrentlyHoveringDismiss = false
        if (immediate) {
            try {
                windowManager?.removeView(container)
            } catch (_: Exception) {}
            return
        }
        try {
            target?.animate()?.cancel()
            target?.animate()
                ?.scaleX(0f)
                ?.scaleY(0f)
                ?.alpha(0f)
                ?.setDuration(150)
                ?.withEndAction {
                    try {
                        windowManager?.removeView(container)
                    } catch (_: Exception) {}
                }
                ?.start() ?: run {
                windowManager?.removeView(container)
            }
        } catch (_: Exception) {
            try {
                windowManager?.removeView(container)
            } catch (_: Exception) {}
        }
    }

    private fun isOverDismissZone(event: MotionEvent): Boolean {
        if (dismissView == null) return false
        val sizePx = dpToPx(DISMISS_SIZE_DP)
        val centerOffsetFromBottom = dpToPx(DISMISS_BOTTOM_MARGIN_DP + DISMISS_SIZE_DP / 2)
        val centerX = szWindow.x / 2f
        val centerY = szWindow.y.toFloat() - centerOffsetFromBottom
        val dx = event.rawX - centerX
        val dy = event.rawY - centerY
        val distance = sqrt(dx * dx + dy * dy)
        val dismissRadiusPx = sizePx * 1.15f
        return distance <= dismissRadiusPx
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val wm = windowManager ?: return false
        val fView = flutterView ?: return false
        if (!windowSetup.enableDrag) return false

        val params = fView.layoutParams as WindowManager.LayoutParams
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTrayAnimationTimer?.cancel()
                mTrayAnimationTimer = null
                mTrayTimerTask?.cancel()
                mTrayTimerTask = null

                dragging = false
                isCurrentlyHoveringDismiss = false
                lastX = event.rawX
                lastY = event.rawY
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY
                if (!dragging && (dx * dx + dy * dy < dragSlopSquared)) {
                    return false
                }
                updateOverlayPosition(params, dx, dy)
                lastX = event.rawX
                lastY = event.rawY
                dragging = true
                if (windowSetup.enableDismiss) {
                    createDismissView()
                    updateDismissProximity(event)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val wasDragging = dragging
                val wasOverDismiss = dragging && windowSetup.enableDismiss && isOverDismissZone(event)
                removeDismissView()
                lastYPosition = params.y
                dragging = false

                if (wasOverDismiss) {
                    cleanupAndStopSelf()
                    return true
                }

                if (windowSetup.positionGravity == "none") {
                    return wasDragging
                }

                startTrayAnimation(params)
                return wasDragging
            }
        }
        return false
    }

    private class DismissDrawable(
        private val size: Int,
        private val backgroundColor: Int,
        iconColor: Int
    ) : Drawable() {
        var isHovered: Boolean = false
            set(value) {
                if (field != value) {
                    field = value
                    circlePaint.color = if (value) 0xFFFF1744.toInt() else backgroundColor
                    ringPaint.color = if (value) 0x66FF1744 else Color.TRANSPARENT
                    invalidateSelf()
                }
            }

        private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.TRANSPARENT
            style = Paint.Style.STROKE
            strokeWidth = size / 10f
        }

        private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }
        private val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = iconColor
            style = Paint.Style.STROKE
            strokeWidth = max(size / 13f, 3f)
            strokeCap = Paint.Cap.ROUND
        }

        override fun draw(canvas: Canvas) {
            val cx = size / 2f
            val cy = size / 2f
            val radius = size / 2.3f
            if (isHovered) {
                canvas.drawCircle(cx, cy, (size / 2f) - (ringPaint.strokeWidth / 2f), ringPaint)
            }
            canvas.drawCircle(cx, cy, radius, circlePaint)
            val arm = radius / 2.2f
            canvas.drawLine(cx - arm, cy - arm, cx + arm, cy + arm, xPaint)
            canvas.drawLine(cx + arm, cy - arm, cx - arm, cy + arm, xPaint)
        }

        override fun setAlpha(alpha: Int) {
            circlePaint.alpha = alpha
            xPaint.alpha = alpha
            ringPaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            circlePaint.colorFilter = colorFilter
            xPaint.colorFilter = colorFilter
            ringPaint.colorFilter = colorFilter
        }

        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        override fun getIntrinsicWidth(): Int = size
        override fun getIntrinsicHeight(): Int = size
    }

    private fun updateOverlayPosition(params: WindowManager.LayoutParams, dx: Float, dy: Float) {
        val invertX = (windowSetup.gravity == (Gravity.TOP or Gravity.RIGHT)
                || windowSetup.gravity == (Gravity.CENTER or Gravity.RIGHT)
                || windowSetup.gravity == (Gravity.BOTTOM or Gravity.RIGHT))
        val invertY = (windowSetup.gravity == (Gravity.BOTTOM or Gravity.LEFT)
                || windowSetup.gravity == Gravity.BOTTOM
                || windowSetup.gravity == (Gravity.BOTTOM or Gravity.RIGHT))

        params.x += ((dx.toInt()) * (if (invertX) -1 else 1))
        params.y += ((dy.toInt()) * (if (invertY) -1 else 1))

        val fView = flutterView
        if (fView != null) {
            applySoftBoundaryConstraints(params, fView.width, fView.height)
            windowManager?.updateViewLayout(fView, params)
        }
    }

    private fun startTrayAnimation(params: WindowManager.LayoutParams) {
        val fView = flutterView ?: return
        windowManager?.updateViewLayout(fView, params)

        mTrayAnimationTimer?.cancel()
        mTrayAnimationTimer = null

        mTrayTimerTask = TrayAnimationTimerTask()
        mTrayAnimationTimer = Timer().apply {
            schedule(mTrayTimerTask, 0, 25)
        }
    }

    private inner class TrayAnimationTimerTask : TimerTask() {
        var mDestX = 0
        var mDestY = lastYPosition
        val mAnimationSpeed = 3.0f
        val mStopThreshold = 2.0f
        val params: WindowManager.LayoutParams = (flutterView?.layoutParams as? WindowManager.LayoutParams)
            ?: WindowManager.LayoutParams()

        init {
            calculateDestinationX()
        }

        private fun calculateDestinationX() {
            val bounds = getSafeBoundaries()
            val leftBound = bounds[0]
            val rightBound = bounds[2]
            val overlayWidth = flutterView?.width ?: 0

            val leftEdgeX: Int
            val rightEdgeX: Int

            if ((windowSetup.gravity and Gravity.RIGHT) == Gravity.RIGHT) {
                rightEdgeX = 0
                leftEdgeX = rightBound - overlayWidth
            } else if ((windowSetup.gravity and Gravity.LEFT) == Gravity.LEFT) {
                leftEdgeX = leftBound
                rightEdgeX = rightBound - overlayWidth
            } else {
                val screenCenterX = szWindow.x / 2
                leftEdgeX = (leftBound + overlayWidth / 2) - screenCenterX
                rightEdgeX = (rightBound - overlayWidth / 2) - screenCenterX
            }

            when (windowSetup.positionGravity) {
                "auto" -> {
                    val currentAbsoluteX = if ((windowSetup.gravity and Gravity.RIGHT) == Gravity.RIGHT) {
                        rightBound - params.x - (overlayWidth / 2)
                    } else if ((windowSetup.gravity and Gravity.LEFT) == Gravity.LEFT) {
                        params.x + (overlayWidth / 2)
                    } else {
                        val screenCenterX = szWindow.x / 2
                        screenCenterX + params.x
                    }
                    val safeAreaCenterX = (leftBound + rightBound) / 2
                    val isOnLeftSide = currentAbsoluteX <= safeAreaCenterX
                    mDestX = if (isOnLeftSide) leftEdgeX else rightEdgeX
                }
                "left" -> mDestX = leftEdgeX
                "right" -> mDestX = rightEdgeX
                else -> {
                    mDestX = params.x
                    mDestY = params.y
                }
            }
        }

        override fun run() {
            mAnimationHandler.post {
                val fView = flutterView ?: return@post
                params.x = (((params.x - mDestX) / mAnimationSpeed) + mDestX).toInt()
                params.y = (((params.y - mDestY) / mAnimationSpeed) + mDestY).toInt()

                constrainToSafeBoundaries(params, fView.width, fView.height)
                windowManager?.updateViewLayout(fView, params)

                if (abs(params.x - mDestX) < mStopThreshold && abs(params.y - mDestY) < mStopThreshold) {
                    params.x = mDestX
                    params.y = mDestY
                    constrainToSafeBoundaries(params, fView.width, fView.height)
                    windowManager?.updateViewLayout(fView, params)
                    cancel()
                    mTrayAnimationTimer?.cancel()
                    mTrayAnimationTimer = null
                }
            }
        }
    }
}
