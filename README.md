<p align="center">
  <img src="https://github.com/X-SLAYER/flutter_overlay_window/assets/22800380/d22ae453-e83d-4da6-ba68-f4eaef666ef1" height="170" alt="flutter_overlay_window logo">
</p>

<p align="center">
  <a href="https://img.shields.io/badge/License-MIT-green">
    <img src="https://img.shields.io/badge/License-MIT-green" alt="MIT License">
  </a>
  <a href="https://github.com/X-SLAYER/flutter_overlay_window">
    <img src="https://img.shields.io/github/stars/X-SLAYER/flutter_overlay_window?style=flat&logo=github&colorB=green&label=stars" alt="stars">
  </a>
  <a href="https://pub.dev/packages/flutter_overlay_window">
    <img src="https://img.shields.io/pub/v/flutter_overlay_window.svg?label=pub&color=orange" alt="pub version">
  </a>
</p>

<p align="center">
Flutter plugin for displaying your Flutter app over other apps on Android (Overlay / Floating Window / Chat-Head).
Migrated to 100% Kotlin with full support for Android 14+ (API 34/35) and Flutter 3.x Built-in Kotlin.
</p>

---

## Previews

| TrueCaller Style Overlay | Click-Through Overlay | Messenger Chat-Head (Draggable) |
| :---: | :---: | :---: |
| <img src="https://user-images.githubusercontent.com/22800380/165636217-8957396b-dc54-4e6d-aa50-e8bfdb9383cf.gif" height="500" width="250" /> | <img src="https://user-images.githubusercontent.com/22800380/165636120-dcd9ee13-5fca-4f8a-a562-b2f53c0b5e24.gif" height="500" width="250" /> | <img src="https://user-images.githubusercontent.com/22800380/178730917-40f267bb-63a2-4ad3-ba69-f7c1285a1882.gif" height="500" width="250" /> |

---

## Installation

Add the dependency to your `pubspec.yaml`:

```yaml
dependencies:
  flutter_overlay_window:
    # Use path or git repository
    git:
      url: https://github.com/breshman/flutter_overlay_window.git
      ref: main
```

---

## Android Configuration (Android 14+ / API 34+ Ready)

In your host application's `android/app/src/main/AndroidManifest.xml`:

1. Add the required permissions inside `<manifest>`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Essential overlay permission -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <!-- Foreground service permissions (Required for Android 14 / API 34+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

    <!-- Notification permission (Required for Android 13 / API 33+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application ...>
        ...

        <!-- Overlay Service declaration with foregroundServiceType and rationale property -->
        <service
            android:name="flutter.overlay.window.flutter_overlay_window.OverlayService"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property
                android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                android:value="Overlay window display over other apps for real-time notifications and controls" />
        </service>

    </application>
</manifest>
```

> **Note on Android 14 (API 34+)**: Google Play requires a description for `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`. Customize the `android:value` above to match your specific app's purpose.

---

## Architecture & Entry Point

The overlay runs in a separate Flutter engine instance on top of Android's `WindowManager`. Therefore, it needs its own top-level entry point function named `overlayMain()` marked with `@pragma("vm:entry-point")`.

### Defining the Entry Point in `lib/main.dart`

```dart
import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MainApp());
}

// -------------------------------------------------------------
// OVERLAY ENTRY POINT
// Must be top-level and annotated with @pragma("vm:entry-point")
// -------------------------------------------------------------
@pragma("vm:entry-point")
void overlayMain() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: MyOverlayWidget(),
    ),
  );
}
```

---

## Real-World Examples

### 1. Basic Flow: Requesting Permissions and Showing the Overlay

```dart
import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

class MainScreen extends StatelessWidget {
  const MainScreen({super.key});

  Future<void> _launchOverlay(BuildContext context) async {
    // 1. Check if permission is already granted
    final bool isGranted = await FlutterOverlayWindow.isPermissionGranted();
    if (!isGranted) {
      // 2. Request permission (opens system overlay settings)
      final bool? requested = await FlutterOverlayWindow.requestPermission();
      if (requested != true) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Overlay permission denied")),
        );
        return;
      }
    }

    // 3. Avoid launching if already active
    if (await FlutterOverlayWindow.isActive()) {
      return;
    }

    // 4. Open the overlay
    await FlutterOverlayWindow.showOverlay(
      height: 220,
      width: 220,
      alignment: OverlayAlignment.center,
      enableDrag: true,
      positionGravity: PositionGravity.auto, // Snaps to screen edges after dragging
      flag: OverlayFlag.defaultFlag,
      overlayTitle: "My App Overlay",
      overlayContent: "Tap to return to app",
      visibility: NotificationVisibility.visibilityPublic,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Overlay Controller")),
      body: Center(
        child: ElevatedButton.icon(
          icon: const Icon(Icons.open_in_new),
          label: const Text("Launch Floating Overlay"),
          onPressed: () => _launchOverlay(context),
        ),
      ),
    );
  }
}
```

---

### 2. Messenger-Style Chat Head (Expandable & Draggable)

This real example shows a circular bubble that expands to a full dialog when tapped, and contracts back to a small floating circle:

```dart
import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

class ChatHeadOverlay extends StatefulWidget {
  const ChatHeadOverlay({super.key});

  @override
  State<ChatHeadOverlay> createState() => _ChatHeadOverlayState();
}

class _ChatHeadOverlayState extends State<ChatHeadOverlay> {
  bool _isExpanded = false;

  Future<void> _toggleExpand() async {
    if (_isExpanded) {
      // Collapse back to a 100x100 draggable bubble
      await FlutterOverlayWindow.resizeOverlay(100, 100, true);
    } else {
      // Expand to full screen (non-draggable while expanded)
      await FlutterOverlayWindow.resizeOverlay(
        WindowSize.matchParent,
        WindowSize.matchParent,
        false,
      );
    }
    setState(() {
      _isExpanded = !_isExpanded;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: Center(
        child: _isExpanded ? _buildExpandedCard() : _buildCircularBubble(),
      ),
    );
  }

  Widget _buildCircularBubble() {
    return GestureDetector(
      onTap: _toggleExpand,
      child: Container(
        width: 80,
        height: 80,
        decoration: BoxDecoration(
          color: Colors.indigoAccent,
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.3),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: const Icon(Icons.chat, color: Colors.white, size: 36),
      ),
    );
  }

  Widget _buildExpandedCard() {
    return Container(
      margin: const EdgeInsets.all(24.0),
      padding: const EdgeInsets.all(16.0),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16.0),
        boxShadow: const [
          BoxShadow(color: Colors.black26, blurRadius: 16),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                "Active Conversation",
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: _toggleExpand,
              ),
            ],
          ),
          const Divider(),
          const Text("Hello! This is a floating overlay running over other apps."),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              TextButton(
                onPressed: () => FlutterOverlayWindow.closeOverlay(),
                child: const Text("Close Overlay", style: TextStyle(color: Colors.red)),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
```

---

### 3. Bidirectional Communication (Main App $\leftrightarrow$ Overlay)

You can pass data back and forth seamlessly between your main app and the overlay window using `shareData` and `overlayListener`:

#### In Main App:
```dart
// Send data to the overlay
await FlutterOverlayWindow.shareData({"action": "update_status", "value": "Delivered"});

// Listen for events from the overlay
FlutterOverlayWindow.overlayListener.listen((data) {
  print("Received from overlay: $data");
});
```

#### In Overlay Widget:
```dart
@override
void initState() {
  super.initState();
  // Listen for messages from main app
  FlutterOverlayWindow.overlayListener.listen((data) {
    if (data is Map && data["action"] == "update_status") {
      setState(() {
        _status = data["value"];
      });
    }
  });
}

// Send a response back to the main app
void _notifyMainApp() {
  FlutterOverlayWindow.shareData("Overlay button clicked!");
}
```

---

### 4. Interactive Overlay with Keyboard / Text Fields

By default, overlays do not take keyboard focus. To show an overlay with a `TextField` that allows keyboard input:

```dart
await FlutterOverlayWindow.showOverlay(
  height: 300,
  width: WindowSize.matchParent,
  alignment: OverlayAlignment.center,
  // Use focusPointer to enable keyboard input on text fields:
  flag: OverlayFlag.focusPointer,
  enableDrag: false,
);
```

---

## API Reference

### `FlutterOverlayWindow` Methods

| Method | Description |
| :--- | :--- |
| `isPermissionGranted()` | Returns `true` if `SYSTEM_ALERT_WINDOW` permission is granted. |
| `requestPermission()` | Opens Android overlay settings screen and awaits user permission. |
| `showOverlay(...)` | Displays the overlay window using the specified parameters. |
| `closeOverlay()` | Closes and removes the currently active overlay window. |
| `isActive()` | Checks whether the overlay is currently visible/active. |
| `shareData(dynamic data)` | Sends data between the main application and the overlay. |
| `overlayListener` | Stream of messages shared between the main app and overlay. |
| `resizeOverlay(width, height, [enableDrag])` | Dynamically resizes the overlay window. |
| `moveOverlay(OverlayPosition position)` | Moves the overlay to specific `x, y` coordinates. |
| `getOverlayPosition()` | Returns the current `OverlayPosition(x, y)` in dp. |
| `updateFlag(OverlayFlag flag)` | Dynamically updates the window flag (e.g. click-through or focusable). |
| `updateNotification({title, content, icon})` | Updates the ongoing foreground service notification. |

### `OverlayFlag` Options

- `OverlayFlag.defaultFlag`: Standard overlay window. Does not capture keyboard focus.
- `OverlayFlag.clickThrough`: Window ignores all touches; touches pass straight to background apps.
- `OverlayFlag.focusPointer`: Window can take keyboard input and focus when needed.

### `PositionGravity` Options

- `PositionGravity.none`: Overlay stays exactly where it was dragged.
- `PositionGravity.auto`: Overlay snaps smoothly to the nearest screen edge (left or right) on release.
- `PositionGravity.left`: Overlay snaps always to the left edge on release.
- `PositionGravity.right`: Overlay snaps always to the right edge on release.

---

## License

MIT License. See [LICENSE](LICENSE) for details.
