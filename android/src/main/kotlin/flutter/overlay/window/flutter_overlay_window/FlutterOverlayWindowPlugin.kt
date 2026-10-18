package flutter.overlay.window.flutter_overlay_window

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.StandardMessageCodec
import androidx.core.net.toUri

class FlutterOverlayWindowPlugin :
    FlutterPlugin,
    ActivityAware,
    BasicMessageChannel.MessageHandler<Any?>,
    MethodChannel.MethodCallHandler,
    PluginRegistry.ActivityResultListener {

    private var channel: MethodChannel? = null
    private var context: Context? = null
    private var mActivity: Activity? = null
    private var messenger: BasicMessageChannel<Any?>? = null
    private val windowSetup = WindowSetup.instance
    private var activityBinding: ActivityPluginBinding? = null
    private var pendingResult: MethodChannel.Result? = null
    private val REQUEST_CODE_FOR_OVERLAY_PERMISSION = 1248

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, OverlayConstants.CHANNEL_TAG)
        channel?.setMethodCallHandler(this)

        messenger = BasicMessageChannel(
            flutterPluginBinding.binaryMessenger,
            OverlayConstants.MESSENGER_TAG,
            StandardMessageCodec.INSTANCE
        )
        messenger?.setMessageHandler(this)

        windowSetup.messenger = messenger
        windowSetup.messenger?.setMessageHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "checkPermission" -> {
                result.success(checkOverlayPermission())
            }
            "requestPermission" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkOverlayPermission()) {
                        result.success(true)
                        return
                    }
                    val activity = mActivity
                    if (activity == null) {
                        result.error("NO_ACTIVITY", "requestPermission requires a foreground activity", null)
                        return
                    }
                    pendingResult?.success(checkOverlayPermission())
                    pendingResult = result
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = "package:${activity.packageName}".toUri()
                    }
                    activity.startActivityForResult(intent, REQUEST_CODE_FOR_OVERLAY_PERMISSION)
                } else {
                    result.success(true)
                }
            }
            "showOverlay" -> {
                if (!checkOverlayPermission()) {
                    result.error("PERMISSION", "overlay permission is not enabled", null)
                    return
                }
                val height: Int? = call.argument("height")
                val width: Int? = call.argument("width")
                val alignment: String? = call.argument("alignment")
                val flag: String? = call.argument("flag")
                val overlayTitle: String? = call.argument("overlayTitle")
                val overlayContent: String? = call.argument("overlayContent")
                val notificationVisibility: String? = call.argument("notificationVisibility")
                val notificationIcon: String? = call.argument("notificationIcon")
                val enableDrag: Boolean = call.argument("enableDrag") ?: false
                val enableDismiss: Boolean = call.argument("enableDismiss") ?: false
                val dismissIcon: String? = call.argument("dismissIcon")
                val dismissIconColor: Number? = call.argument("dismissIconColor")
                val dismissBackgroundColor: Number? = call.argument("dismissBackgroundColor")
                val positionGravity: String? = call.argument("positionGravity")
                val startPosition: Map<String, Int>? = call.argument("startPosition")
                val startX = startPosition?.get("x") ?: OverlayConstants.DEFAULT_XY
                val startY = startPosition?.get("y") ?: OverlayConstants.DEFAULT_XY

                windowSetup.width = width ?: -1
                windowSetup.height = height ?: -1
                windowSetup.enableDrag = enableDrag
                windowSetup.enableDismiss = enableDismiss
                windowSetup.dismissIcon = dismissIcon
                if (dismissIconColor != null) windowSetup.dismissIconColor = dismissIconColor.toInt()
                if (dismissBackgroundColor != null) windowSetup.dismissBackgroundColor = dismissBackgroundColor.toInt()
                windowSetup.setGravityFromAlignment(alignment ?: "center")
                windowSetup.setFlag(flag ?: "flagNotFocusable")
                windowSetup.overlayTitle = overlayTitle ?: "Overlay is activated"
                windowSetup.overlayContent = overlayContent ?: ""
                windowSetup.positionGravity = positionGravity ?: "none"
                windowSetup.setNotificationVisibility(notificationVisibility)
                windowSetup.notificationIcon = notificationIcon

                val ctx = context
                if (ctx != null) {
                    val intent = Intent(ctx, OverlayService::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("startX", startX)
                        putExtra("startY", startY)
                        putExtra("width", width ?: -1)
                        putExtra("height", height ?: -1)
                        putExtra("alignment", alignment ?: "center")
                        putExtra("flag", flag ?: "flagNotFocusable")
                        putExtra("overlayTitle", overlayTitle ?: "Overlay is activated")
                        putExtra("overlayContent", overlayContent ?: "")
                        putExtra("enableDrag", enableDrag)
                        putExtra(OverlayConstants.ENABLE_DISMISS, enableDismiss)
                        if (dismissIcon != null) putExtra(OverlayConstants.DISMISS_ICON, dismissIcon)
                        if (dismissIconColor != null) putExtra(OverlayConstants.DISMISS_ICON_COLOR, dismissIconColor.toInt())
                        if (dismissBackgroundColor != null) putExtra(OverlayConstants.DISMISS_BG_COLOR, dismissBackgroundColor.toInt())
                        putExtra("positionGravity", positionGravity ?: "none")
                        if (notificationVisibility != null) putExtra("notificationVisibility", notificationVisibility)
                        if (notificationIcon != null) putExtra("notificationIcon", notificationIcon)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ctx.startForegroundService(intent)
                    } else {
                        ctx.startService(intent)
                    }
                    result.success(null)
                } else {
                    result.error("NO_CONTEXT", "Plugin context is null", null)
                }
            }
            "updateNotification" -> {
                val title: String? = call.argument("title")
                val content: String? = call.argument("content")
                val icon: String? = call.argument("icon")
                if (title != null) windowSetup.overlayTitle = title
                if (content != null) windowSetup.overlayContent = content
                if (icon != null) windowSetup.notificationIcon = icon
                result.success(OverlayService.updateNotification())
            }
            "isOverlayActive" -> {
                result.success(OverlayService.isRunning)
            }
            "moveOverlay" -> {
                val x: Int? = call.argument("x")
                val y: Int? = call.argument("y")
                if (x != null && y != null) {
                    result.success(OverlayService.moveOverlay(x, y))
                } else {
                    result.success(false)
                }
            }
            "getOverlayPosition" -> {
                result.success(OverlayService.getCurrentPosition())
            }
            "closeOverlay" -> {
                if (OverlayService.isRunning) {
                    val ctx = context
                    if (ctx != null) {
                        val intent = Intent(ctx, OverlayService::class.java)
                        ctx.stopService(intent)
                        result.success(true)
                    } else {
                        result.success(false)
                    }
                } else {
                    result.success(false)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
        windowSetup.messenger?.setMessageHandler(null)
        messenger = null
    }

    override fun onAttachedToActivity(@NonNull binding: ActivityPluginBinding) {
        attachToActivity(binding)
        val ctx = context ?: binding.activity.applicationContext
        if (FlutterEngineCache.getInstance().get(OverlayConstants.CACHED_TAG) == null) {
            val engineGroup = FlutterEngineGroup(ctx)
            val dEntry = DartExecutor.DartEntrypoint(
                FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                "overlayMain"
            )
            val engine = engineGroup.createAndRunEngine(ctx, dEntry)
            FlutterEngineCache.getInstance().put(OverlayConstants.CACHED_TAG, engine)
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        detachFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(@NonNull binding: ActivityPluginBinding) {
        attachToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        detachFromActivity()
    }

    private fun attachToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        mActivity = binding.activity
        binding.addActivityResultListener(this)
    }

    private fun detachFromActivity() {
        activityBinding?.removeActivityResultListener(this)
        activityBinding = null
        mActivity = null
    }

    override fun onMessage(message: Any?, reply: BasicMessageChannel.Reply<Any?>) {
        val cachedEngine = FlutterEngineCache.getInstance().get(OverlayConstants.CACHED_TAG)
        if (cachedEngine != null) {
            val overlayMessageChannel = BasicMessageChannel(
                cachedEngine.dartExecutor,
                OverlayConstants.MESSENGER_TAG,
                StandardMessageCodec.INSTANCE
            )
            overlayMessageChannel.send(message, reply)
        }
    }

    private fun checkOverlayPermission(): Boolean {
        val ctx = context ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(ctx)
        } else {
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_CODE_FOR_OVERLAY_PERMISSION) {
            pendingResult?.let {
                it.success(checkOverlayPermission())
                pendingResult = null
            }
            return true
        }
        return false
    }
}
