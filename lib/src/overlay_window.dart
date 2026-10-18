import 'dart:async';
import 'dart:developer';

import 'package:flutter/services.dart';
import 'package:flutter_overlay_window/src/models/overlay_position.dart';
import 'package:flutter_overlay_window/src/overlay_config.dart';

class FlutterOverlayWindow {
  FlutterOverlayWindow._();

  static StreamController<dynamic>? _controller;
  static const MethodChannel _channel =
      MethodChannel("x-slayer/overlay_channel");
  static const MethodChannel _overlayChannel =
      MethodChannel("x-slayer/overlay");
  static const BasicMessageChannel<dynamic> _overlayMessageChannel =
      BasicMessageChannel<dynamic>(
    "x-slayer/overlay_messenger",
    StandardMessageCodec(),
  );

  static StreamController<dynamic> get _ensureController {
    if (_controller == null || _controller!.isClosed) {
      _controller = StreamController<dynamic>.broadcast();
      _overlayMessageChannel.setMessageHandler((message) async {
        if (_controller != null && !_controller!.isClosed) {
          _controller!.add(message);
        }
        return message;
      });
    }
    return _controller!;
  }

  /// Open overLay content
  ///
  /// - Optional arguments:
  ///
  /// `height` the overlay height and default is [WindowSize.fullCover]
  ///
  /// `width` the overlay width and default is [WindowSize.matchParent]
  ///
  /// `alignment` the alignment postion on screen and default is [OverlayAlignment.center]
  ///
  /// `visibilitySecret` the detail displayed in notifications on the lock screen and default is [NotificationVisibility.visibilitySecret]
  ///
  /// `OverlayFlag` the overlay flag and default is [OverlayFlag.defaultFlag]
  ///
  /// `overlayTitle` the notification message and default is "overlay activated"
  ///
  /// `overlayContent` the notification message
  ///
  /// `enableDrag` to enable/disable dragging the overlay over the screen and default is "false"
  ///
  /// `enableDismiss` to enable drag-to-dismiss: a dismiss target appears at bottom-center
  /// during drag; dropping the overlay over it closes the overlay. Default is false.
  ///
  /// `dismissIcon` the name of a drawable or mipmap resource used as the dismiss zone icon.
  /// Falls back to a built-in X-in-circle if not set or not found.
  ///
  /// `dismissIconColor` color of the X icon in the built-in dismiss zone (supports [Color] or ARGB [int]).
  ///
  /// `dismissBackgroundColor` background color of the built-in dismiss circle (supports [Color] or ARGB [int]).
  static Future<void> showOverlay({
    int height = WindowSize.fullCover,
    int width = WindowSize.matchParent,
    OverlayAlignment alignment = OverlayAlignment.center,
    NotificationVisibility visibility = NotificationVisibility.visibilitySecret,
    OverlayFlag flag = OverlayFlag.defaultFlag,
    String overlayTitle = "overlay activated",
    String? overlayContent,
    bool enableDrag = false,
    bool enableDismiss = false,
    dynamic dismissIconColor,
    dynamic dismissBackgroundColor,
    String? dismissIcon,
    PositionGravity positionGravity = PositionGravity.none,
    OverlayPosition? startPosition,
    String? notificationIcon,
  }) async {
    int? resolvedIconColor;
    if (dismissIconColor is Color) {
      resolvedIconColor = dismissIconColor.toARGB32();
    } else if (dismissIconColor is int) {
      resolvedIconColor = dismissIconColor;
    }

    int? resolvedBgColor;
    if (dismissBackgroundColor is Color) {
      resolvedBgColor = dismissBackgroundColor.toARGB32();
    } else if (dismissBackgroundColor is int) {
      resolvedBgColor = dismissBackgroundColor;
    }

    await _channel.invokeMethod(
      'showOverlay',
      {
        "height": height,
        "width": width,
        "alignment": alignment.name,
        "flag": flag.name,
        "overlayTitle": overlayTitle,
        "overlayContent": overlayContent,
        "enableDrag": enableDrag,
        "enableDismiss": enableDismiss,
        if (resolvedIconColor != null) "dismissIconColor": resolvedIconColor,
        if (resolvedBgColor != null)
          "dismissBackgroundColor": resolvedBgColor,
        if (dismissIcon != null) "dismissIcon": dismissIcon,
        "notificationVisibility": visibility.name,
        "positionGravity": positionGravity.name,
        "startPosition": startPosition?.toMap(),
        "notificationIcon": notificationIcon,
      },
    );
  }

  /// Update the foreground notification while the overlay is showing.
  ///
  /// Only the provided values are changed. `icon` follows the same rules as
  /// `notificationIcon` in [showOverlay].
  ///
  /// Returns `true` if the notification was updated, `false` if no overlay is active.
  static Future<bool> updateNotification({
    String? title,
    String? content,
    String? icon,
  }) async {
    final bool? res = await _channel.invokeMethod<bool>(
      'updateNotification',
      {"title": title, "content": content, "icon": icon},
    );
    return res ?? false;
  }

  /// Check if overlay permission is granted
  static Future<bool> isPermissionGranted() async {
    try {
      return await _channel.invokeMethod<bool>('checkPermission') ?? false;
    } on PlatformException catch (error) {
      log("$error");
      return false;
    }
  }

  /// Request overlay permission
  /// it will open the overlay settings page and return `true` once the permission granted.
  static Future<bool?> requestPermission() async {
    try {
      return await _channel.invokeMethod<bool?>('requestPermission');
    } on PlatformException catch (error) {
      log("Error requestPermission: $error");
      rethrow;
    }
  }

  /// Closes overlay if open
  static Future<bool?> closeOverlay() async {
    final bool? res = await _channel.invokeMethod('closeOverlay');
    return res;
  }

  /// Broadcast data to and from overlay app
  static Future shareData(dynamic data) async {
    return await _overlayMessageChannel.send(data);
  }

  /// Streams message shared between overlay and main app (broadcast stream)
  static Stream<dynamic> get overlayListener {
    return _ensureController.stream;
  }

  /// Update the overlay flag while the overlay in action
  static Future<bool?> updateFlag(OverlayFlag flag) async {
    final bool? res = await _overlayChannel
        .invokeMethod<bool?>('updateFlag', {'flag': flag.name});
    return res;
  }

  /// Update the overlay size in the screen
  static Future<bool?> resizeOverlay(
    int width,
    int height,
    bool enableDrag,
  ) async {
    final bool? res = await _overlayChannel.invokeMethod<bool?>(
      'resizeOverlay',
      {
        'width': width,
        'height': height,
        'enableDrag': enableDrag,
      },
    );
    return res;
  }

  /// Update the overlay position in the screen
  ///
  /// `position` the new position of the overlay
  ///
  /// `return` true if the position updated successfully
  static Future<bool?> moveOverlay(OverlayPosition position) async {
    final bool? res = await _channel.invokeMethod<bool?>(
      'moveOverlay',
      position.toMap(),
    );
    return res;
  }

  /// Get the current overlay position
  ///
  /// `return` the current overlay position
  static Future<OverlayPosition> getOverlayPosition() async {
    final Map<Object?, Object?>? res = await _channel.invokeMethod(
      'getOverlayPosition',
    );
    return OverlayPosition.fromMap(res);
  }

  /// Check if the current overlay is active
  static Future<bool> isActive() async {
    final bool? res = await _channel.invokeMethod<bool?>('isOverlayActive');
    return res ?? false;
  }

  /// Dispose overlay stream controller
  static void disposeOverlayListener() {
    _controller?.close();
    _controller = null;
  }
}
