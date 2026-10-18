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
  <img src="https://img.shields.io/badge/Android-14%20%7C%2015%20(API%2034%2F35)-blue" alt="Android 14/15 Ready">
  <img src="https://img.shields.io/badge/Kotlin-100%25-purple" alt="100% Kotlin">
  <img src="https://img.shields.io/badge/Tests-18%20passing-brightgreen" alt="18 Tests Passing">
</p>

<p align="center">
A production-ready Flutter plugin for displaying high-performance floating windows and overlay widgets over other applications on Android (Chat-Heads, Dynamic Island capsules, Quick Notepads, VIP Caller HUDs).
</p>

---

## ⚡ What's New in 2.0.0

- **100% Kotlin Migration**: Rewritten entirely from Java to Kotlin (`FlutterOverlayWindowPlugin`, `OverlayService`, `WindowSetup`).
- **Android 14 & 15 Ready**: Full compliance with Foreground Service types (`FOREGROUND_SERVICE_SPECIAL_USE`) and Android 13+ notifications (`POST_NOTIFICATIONS`).
- **Gradle 8/9 & AGP 8/9 Built-in Kotlin**: Zero conflicts with modern host apps. No hardcoded repository URLs or legacy plugin collisions (`FAIL_ON_PROJECT_REPOS`).
- **Screen Rotation Auto-Recovery**: Native `onConfigurationChanged` dynamically repositions and bounds overlays during Portrait $\leftrightarrow$ Landscape switches.
- **Broadcast Stream Controller**: `overlayListener` now uses a broadcast stream supporting multiple listeners and safe stream re-subscription via `disposeOverlayListener()`.
- **StandardMessageCodec**: High-performance binary serialization for structured two-way communication (Primitives, Maps, Lists, nested payloads).
- **Decoupled Intent Parameters**: Service configuration is passed via `Intent.extras`, eliminating memory leaks and singleton state coupling.
- **4 Real-World Showcase Overlays**: Ready-to-use patterns for Dynamic Island, Messenger Chat-Head, Quick Note with Keyboard, and Caller HUD.

---

## 📱 Previews

| Dynamic Island Pill / Player | Messenger Chat-Head & Expansion | Quick Note with Keyboard | VIP Caller HUD Banner |
| :---: | :---: | :---: | :---: |
| Top capsule expands into audio player with sliders | Floating bubble with badge snaps to screen edge | Note pad with soft-keyboard & color switcher | Frosted glass HUD with Accept/Decline actions |

---

## 📦 Installation

Add the dependency to your `pubspec.yaml`:

```yaml
dependencies:
  flutter_overlay_window:
    git:
      url: https://github.com/breshman/flutter_overlay_window.git
      ref: main
```

Or when published to pub.dev:

```yaml
dependencies:
  flutter_overlay_window: ^2.0.0
```

---

## 🛠️ Android Configuration (Android 14+ / API 34+ Ready)

In your host application's `android/app/src/main/AndroidManifest.xml`:

### 1. Declare Permissions
Add the following permissions inside `<manifest>`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Essential overlay permissions -->
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

> **Google Play Note**: Android 14 requires a justification description for `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`. Customize the `android:value` above to match your specific app's purpose.

---

## 🏗️ Architecture & Entry Point

The overlay runs in an **isolated Flutter Engine instance** managed by Android's native `WindowManager`. It requires its own top-level entry point function named `overlayMain()` annotated with `@pragma("vm:entry-point")`.

### Defining the Entry Points in `lib/main.dart`

```dart
import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

// Main Application Entry Point
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
      home: OverlayDispatcher(), // Central router for overlay designs
    ),
  );
}
```

---

## 🎨 Showcase & Real-World Examples

All of these production designs are included in the [`example`](example/) folder:

### 1. Centralized Multi-Overlay Router (`OverlayDispatcher`)
Instead of hardcoding a single overlay widget, use `OverlayDispatcher` to switch between different designs on the fly using `overlayListener`:

```dart
class OverlayDispatcher extends StatefulWidget {
  const OverlayDispatcher({super.key});

  @override
  State<OverlayDispatcher> createState() => _OverlayDispatcherState();
}

class _OverlayDispatcherState extends State<OverlayDispatcher> {
  String _activeDesign = "chathead";

  @override
  void initState() {
    super.initState();
    FlutterOverlayWindow.overlayListener.listen((data) {
      if (data is Map && data.containsKey("design")) {
        setState(() => _activeDesign = data["design"] as String);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    switch (_activeDesign) {
      case "dynamic_island":
        return const DynamicIslandOverlay();
      case "quick_note":
        return const QuickNoteOverlay();
      case "true_caller":
        return const TrueCallerOverlay();
      case "chathead":
      default:
        return const MessangerChatHead();
    }
  }
}
```

---

### 2. Messenger Chat-Head (Draggable Bubble with Expandable Card)

Floating draggable bubble with unread badge counter that smoothly snaps to screen edges and expands into a chat card:

```dart
class MessangerChatHead extends StatefulWidget {
  const MessangerChatHead({super.key});

  @override
  State<MessangerChatHead> createState() => _MessangerChatHeadState();
}

class _MessangerChatHeadState extends State<MessangerChatHead> {
  bool _isExpanded = false;

  Future<void> _toggleExpand() async {
    if (_isExpanded) {
      // Contract back to draggable 110x110 bubble
      await FlutterOverlayWindow.resizeOverlay(110, 110, true);
    } else {
      // Expand to full card size and disable dragging while open
      await FlutterOverlayWindow.resizeOverlay(340, 420, false);
    }
    setState(() => _isExpanded = !_isExpanded);
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: Center(
        child: _isExpanded ? _buildChatCard() : _buildBubble(),
      ),
    );
  }

  Widget _buildBubble() {
    return GestureDetector(
      onTap: _toggleExpand,
      child: Stack(
        alignment: Alignment.center,
        children: [
          Container(
            width: 72,
            height: 72,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(colors: [Color(0xFF0072FF), Color(0xFF00C6FF)]),
              boxShadow: [BoxShadow(color: Colors.black38, blurRadius: 10, offset: Offset(0, 4))],
            ),
            child: const Icon(Icons.forum_rounded, color: Colors.white, size: 34),
          ),
          Positioned(
            right: 2,
            top: 2,
            child: Container(
              padding: const EdgeInsets.all(5),
              decoration: const BoxDecoration(color: Colors.redAccent, shape: BoxShape.circle),
              child: const Text("3", style: TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold)),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildChatCard() {
    return Container(
      width: 320,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: const [BoxShadow(color: Colors.black26, blurRadius: 16)],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text("Support Chat", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              IconButton(icon: const Icon(Icons.close), onPressed: _toggleExpand),
            ],
          ),
          const Divider(),
          const Text("Hello! How can we help you today?"),
          const SizedBox(height: 12),
          ElevatedButton(
            onPressed: () {
              FlutterOverlayWindow.shareData("reply_sent");
              _toggleExpand();
            },
            child: const Text("Send Quick Reply"),
          ),
        ],
      ),
    );
  }
}
```

---

### 3. Dynamic Island Capsule (Live Activity Media Player)

A compact pill anchored at the top that smoothly animates into a media player card with live playback controls:

```dart
class DynamicIslandOverlay extends StatefulWidget {
  const DynamicIslandOverlay({super.key});

  @override
  State<DynamicIslandOverlay> createState() => _DynamicIslandOverlayState();
}

class _DynamicIslandOverlayState extends State<DynamicIslandOverlay> {
  bool _isExpanded = false;
  bool _isPlaying = true;
  double _progress = 0.45;

  Future<void> _toggleExpand() async {
    if (_isExpanded) {
      await FlutterOverlayWindow.resizeOverlay(200, 56, true);
    } else {
      await FlutterOverlayWindow.resizeOverlay(340, 240, false);
    }
    setState(() => _isExpanded = !_isExpanded);
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: Center(
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 250),
          width: _isExpanded ? 340 : 200,
          height: _isExpanded ? 240 : 56,
          decoration: BoxDecoration(
            color: const Color(0xFF121316).withValues(alpha: 0.95),
            borderRadius: BorderRadius.circular(28),
            border: Border.all(color: Colors.white12),
            boxShadow: const [BoxShadow(color: Colors.black45, blurRadius: 16)],
          ),
          child: _isExpanded ? _buildExpandedPlayer() : _buildCompactPill(),
        ),
      ),
    );
  }

  Widget _buildCompactPill() {
    return GestureDetector(
      onTap: _toggleExpand,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          const Icon(Icons.music_note, color: Colors.orangeAccent),
          const Text("Starboy - The Weeknd", style: TextStyle(color: Colors.white, fontSize: 12)),
          IconButton(
            icon: Icon(_isPlaying ? Icons.pause : Icons.play_arrow, color: Colors.white),
            onPressed: () => setState(() => _isPlaying = !_isPlaying),
          ),
        ],
      ),
    );
  }

  Widget _buildExpandedPlayer() {
    return Column(
      children: [
        ListTile(
          leading: const Icon(Icons.album, color: Colors.orangeAccent, size: 40),
          title: const Text("Starboy", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          subtitle: const Text("The Weeknd • Daft Punk", style: TextStyle(color: Colors.white70)),
          trailing: IconButton(icon: const Icon(Icons.close_fullscreen, color: Colors.white70), onPressed: _toggleExpand),
        ),
        Slider(
          value: _progress,
          onChanged: (val) => setState(() => _progress = val),
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            IconButton(icon: const Icon(Icons.skip_previous, color: Colors.white), onPressed: () {}),
            IconButton(
              icon: Icon(_isPlaying ? Icons.pause_circle_filled : Icons.play_circle_fill, color: Colors.white, size: 36),
              onPressed: () => setState(() => _isPlaying = !_isPlaying),
            ),
            IconButton(icon: const Icon(Icons.skip_next, color: Colors.white), onPressed: () {}),
          ],
        ),
      ],
    );
  }
}
```

---

### 4. Quick Note Pad with Keyboard Input (`OverlayFlag.focusPointer`)

Overlays normally ignore keyboard focus so touches can fall through. Use `OverlayFlag.focusPointer` to enable keyboard input on floating text fields:

```dart
class QuickNoteOverlay extends StatefulWidget {
  const QuickNoteOverlay({super.key});

  @override
  State<QuickNoteOverlay> createState() => _QuickNoteOverlayState();
}

class _QuickNoteOverlayState extends State<QuickNoteOverlay> {
  final TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Focus(
      onFocusChange: (hasFocus) async {
        // Toggle focusable flag on focus change to manage keyboard interaction
        await FlutterOverlayWindow.updateFlag(
          hasFocus ? OverlayFlag.focusPointer : OverlayFlag.defaultFlag,
        );
      },
      child: Material(
        color: Colors.transparent,
        child: Center(
          child: Container(
            width: 320,
            height: 260,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFFFEF3C7),
              borderRadius: BorderRadius.circular(18),
              boxShadow: const [BoxShadow(color: Colors.black26, blurRadius: 14)],
            ),
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text("Quick Note", style: TextStyle(fontWeight: FontWeight.bold)),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => FlutterOverlayWindow.closeOverlay(),
                    ),
                  ],
                ),
                Expanded(
                  child: TextField(
                    controller: _controller,
                    maxLines: null,
                    expands: true,
                    decoration: const InputDecoration(
                      hintText: "Type note...",
                      border: InputBorder.none,
                    ),
                  ),
                ),
                ElevatedButton(
                  onPressed: () {
                    // Send note back to the main app
                    FlutterOverlayWindow.shareData({
                      "type": "note_saved",
                      "content": _controller.text,
                    });
                    FlutterOverlayWindow.closeOverlay();
                  },
                  child: const Text("Save & Send"),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
```

---

### 5. VIP Caller HUD Banner

Frosted-glass style incoming call alert with action buttons:

```dart
class TrueCallerOverlay extends StatelessWidget {
  const TrueCallerOverlay({super.key});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: Center(
        child: Container(
          width: 340,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              colors: [Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)],
            ),
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: Colors.white24),
            boxShadow: const [BoxShadow(color: Colors.black45, blurRadius: 18)],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const ListTile(
                leading: CircleAvatar(
                  backgroundColor: Colors.amber,
                  child: Icon(Icons.person, color: Colors.black),
                ),
                title: Text("VIP Client", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                subtitle: Text("+34 612 345 678 • Enterprise SLA", style: TextStyle(color: Colors.white70)),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(backgroundColor: Colors.redAccent),
                      icon: const Icon(Icons.call_end, color: Colors.white),
                      label: const Text("Decline", style: TextStyle(color: Colors.white)),
                      onPressed: () {
                        FlutterOverlayWindow.shareData("call_declined");
                        FlutterOverlayWindow.closeOverlay();
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(backgroundColor: Colors.green),
                      icon: const Icon(Icons.call, color: Colors.white),
                      label: const Text("Answer", style: TextStyle(color: Colors.white)),
                      onPressed: () {
                        FlutterOverlayWindow.shareData("call_accepted");
                        FlutterOverlayWindow.closeOverlay();
                      },
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
```

---

## 🔄 Bidirectional Communication

Pass rich structured data back and forth between your main application and the overlay using `shareData` and `overlayListener` (backed by `StandardMessageCodec`):

### Main App $\rightarrow$ Overlay

```dart
// Send a structured map
await FlutterOverlayWindow.shareData({
  "design": "dynamic_island",
  "track": "Blinding Lights",
  "artist": "The Weeknd",
});
```

### Overlay $\rightarrow$ Main App

```dart
// Inside Overlay Widget
await FlutterOverlayWindow.shareData({
  "event": "button_clicked",
  "timestamp": DateTime.now().millisecondsSinceEpoch,
});
```

### Listening to Events

```dart
// Subscribe anywhere (Broadcast Stream supports multiple listeners)
final subscription = FlutterOverlayWindow.overlayListener.listen((data) {
  print("Event received: $data");
});

// To close stream and free resources:
FlutterOverlayWindow.disposeOverlayListener();
```

---

## 📱 Screen Rotation Auto-Recovery

On Android, changing the device orientation (Portrait $\leftrightarrow$ Landscape) normally causes overlay windows to get clipped, stretched, or placed offscreen.

`flutter_overlay_window` includes native `onConfigurationChanged` handling:
- **Automatic Boundary Recalculation**: Screen dimensions are re-queried instantly from `WindowManager`.
- **Clamp & Safe Margins**: Overlays maintain their relative screen position without exceeding screen limits.
- **Zero Engine Restarts**: The Flutter Engine and overlay state are preserved across orientation changes.

---

## 📚 API Reference

### `FlutterOverlayWindow` Methods

| Method | Return Type | Description |
| :--- | :--- | :--- |
| `isPermissionGranted()` | `Future<bool>` | Checks if `SYSTEM_ALERT_WINDOW` permission is granted. |
| `requestPermission()` | `Future<bool?>` | Opens system settings page and awaits permission result. |
| `showOverlay(...)` | `Future<void>` | Spawns the overlay window with configuration parameters. |
| `closeOverlay()` | `Future<bool?>` | Closes and removes the currently active overlay. |
| `isActive()` | `Future<bool>` | Checks whether the overlay is currently open and visible. |
| `shareData(dynamic data)` | `Future<void>` | Sends structured data via `StandardMessageCodec`. |
| `overlayListener` | `Stream<dynamic>` | Broadcast stream of messages shared between app and overlay. |
| `disposeOverlayListener()` | `void` | Closes the broadcast stream controller safely. |
| `resizeOverlay(w, h, [enableDrag])` | `Future<bool?>` | Dynamically updates overlay dimensions and dragability. |
| `moveOverlay(OverlayPosition pos)` | `Future<bool?>` | Moves overlay to specific `(x, y)` coordinate in dp. |
| `getOverlayPosition()` | `Future<OverlayPosition>` | Returns current `(x, y)` position of the overlay. |
| `updateFlag(OverlayFlag flag)` | `Future<bool?>` | Updates window flags (e.g. keyboard focus or click-through). |
| `updateNotification({title, content, icon})` | `Future<bool?>` | Updates foreground notification icon, title, and content. |

### Parameters for `showOverlay(...)`

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `height` | `int` | `WindowSize.fullCover` | Window height in dp or `WindowSize.matchParent` / `fullCover`. |
| `width` | `int` | `WindowSize.matchParent` | Window width in dp or `WindowSize.matchParent` / `fullCover`. |
| `alignment` | `OverlayAlignment` | `OverlayAlignment.center` | Initial gravity anchor point on the screen. |
| `visibility` | `NotificationVisibility` | `visibilitySecret` | Foreground service notification visibility on lock screen. |
| `flag` | `OverlayFlag` | `OverlayFlag.defaultFlag` | Window focus and touch behavior. |
| `overlayTitle` | `String` | `"Overlay Name"` | Notification title. |
| `overlayContent` | `String` | `""` | Notification body content text. |
| `enableDrag` | `bool` | `false` | Enables drag gesture repositioning. |
| `positionGravity` | `PositionGravity` | `PositionGravity.none` | Auto-snap behavior when released after drag. |
| `startPosition` | `OverlayPosition?` | `null` | Custom initial `(x, y)` position. |
| `notificationIcon` | `String?` | `null` | Drawable/mipmap icon resource name. |

---

### Enums

#### `OverlayFlag`
- `OverlayFlag.defaultFlag`: Standard overlay window. Does not capture keyboard focus.
- `OverlayFlag.clickThrough`: Window ignores all touch events; touches pass to background apps.
- `OverlayFlag.focusPointer`: Window takes keyboard input and focus when requested.

#### `PositionGravity`
- `PositionGravity.none`: Overlay stays exactly where it was dragged.
- `PositionGravity.auto`: Automatically snaps to the closest screen edge (left or right).
- `PositionGravity.left`: Always snaps to the left screen edge.
- `PositionGravity.right`: Always snaps to the right screen edge.

---

## 🧪 Unit Testing

Run the comprehensive unit test suite:

```bash
flutter test
```

All 18 tests cover `FlutterOverlayWindow` channels, broadcast stream lifecycle, `OverlayPosition`, `WindowSize`, and enum serialization.

---

## 📄 License

MIT License. See [LICENSE](LICENSE) for details.
