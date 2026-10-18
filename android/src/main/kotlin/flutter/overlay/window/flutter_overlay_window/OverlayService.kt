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
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import io.flutter.FlutterInjector
import io.flutter.embedding.android.FlutterTextureView
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.JSONMessageCodec
import io.flutter.plugin.common.MethodChannel
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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
                JSONMessageCodec.INSTANCE
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

        if (windowManager != null) {
            cleanupAndStopSelf()
        }

        val startX = intent.getIntExtra("startX", OverlayConstants.DEFAULT_XY)
        val startY = intent.getIntExtra("startY", OverlayConstants.DEFAULT_XY)

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
                    val flag: String? = call.argument("flag")
                    updateOverlayFlag(result, flag)
                }
                "updateOverlayPosition" -> {
                    val x: Int = call.argument("x") ?: 0
                    val y: Int = call.argument("y") ?: 0
                    moveOverlay(x, y, result)
                }
                "resizeOverlay" -> {
                    val width: Int = call.argument("width") ?: -1
                    val height: Int = call.argument("height") ?: -1
                    val enableDrag: Boolean = call.argument("enableDrag") ?: false
                    resizeOverlay(width, height, enableDrag, result)
                }
            }
        }

        overlayMessageChannel?.setMessageHandler { message, _ ->
            windowSetup.messenger?.send(message)
        }

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets
            val bounds = windowMetrics.bounds
            szWindow.set(bounds.width(), bounds.height())
        } else {
            val display = wm.defaultDisplay
            val dm = DisplayMetrics()
            display.getRealMetrics(dm)
            szWindow.set(dm.widthPixels, dm.heightPixels)
        }

        val dx = if (startX == OverlayConstants.DEFAULT_XY) 0 else startX
        val dy = if (startY == OverlayConstants.DEFAULT_XY) -statusBarHeightPx() else startY

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            if (windowSetup.width == -1999) -1 else windowSetup.width,
            if (windowSetup.height != -1999) windowSetup.height else screenHeight(),
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

    private fun cleanupAndStopSelf() {
        if (windowManager != null && flutterView != null) {
            windowManager?.removeView(flutterView)
            flutterView?.detachFromFlutterEngine()
            windowManager = null
            flutterView = null
        }
        isRunning = false
        stopSelf()
    }

    override fun onDestroy() {
        Log.d("OverLay", "Destroying the overlay window service")
        mTrayAnimationTimer?.cancel()
        mTrayAnimationTimer = null
        mTrayTimerTask?.cancel()
        mTrayTimerTask = null

        if (windowManager != null && flutterView != null) {
            windowManager?.removeView(flutterView)
            flutterView?.detachFromFlutterEngine()
            windowManager = null
            flutterView = null
        }
        isRunning = false
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(OverlayConstants.NOTIFICATION_ID)
        instance = null
        super.onDestroy()
    }

    private fun screenHeight(): Int {
        if (mScreenHeight == -1) {
            val wm = windowManager ?: return szWindow.y
            val display = wm.defaultDisplay
            val dm = DisplayMetrics()
            display.getRealMetrics(dm)
            mScreenHeight = if (inPortrait()) {
                dm.heightPixels + statusBarHeightPx() + navigationBarHeightPx()
            } else {
                dm.heightPixels + statusBarHeightPx()
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
            params.x = max(maxLeftOffset, params.x)
        } else if ((windowSetup.gravity and Gravity.LEFT) == Gravity.LEFT) {
            val maxRightOffset = leftBound + overlayWidth - minVisibleWidth
            params.x = min(maxRightOffset, params.x)
        } else {
            val maxLeftOffset = leftBound - (szWindow.x / 2) + minVisibleWidth
            val maxRightOffset = rightBound - (szWindow.x / 2) - (overlayWidth - minVisibleWidth)
            params.x = max(maxLeftOffset, min(params.x, maxRightOffset))
        }

        if ((windowSetup.gravity and Gravity.BOTTOM) == Gravity.BOTTOM) {
            val maxTopOffset = bottomBound - minVisibleHeight
            params.y = max(maxTopOffset, params.y)
        } else if ((windowSetup.gravity and Gravity.TOP) == Gravity.TOP) {
            val maxBottomOffset = topBound + overlayHeight - minVisibleHeight
            params.y = min(maxBottomOffset, params.y)
        } else {
            val maxTopOffset = topBound - (szWindow.y / 2) + minVisibleHeight
            val maxBottomOffset = bottomBound - (szWindow.y / 2) - (overlayHeight - minVisibleHeight)
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
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                lastYPosition = params.y
                val wasDragging = dragging
                dragging = false

                if (windowSetup.positionGravity == "none") {
                    return wasDragging
                }

                startTrayAnimation(params)
                return wasDragging
            }
        }
        return false
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
