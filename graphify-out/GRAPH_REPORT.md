# Graph Report - flutter_overlay_window  (2026-10-17)

## Corpus Check
- 34 files · ~13,369 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 317 nodes · 403 edges · 25 communities (19 shown, 6 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `235243c8`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Android Overlay Service
- Android Plugin Lifecycle & Activity
- MethodChannel & Notifications Handler
- Flutter Plugin Dart API
- Example Home Page & Isolate Port
- Example Messenger Chathead Overlay
- Overlay Configuration & Alignment
- Example Entry Point & App Lifecycle
- Overlay Position Models
- Example TrueCaller Style Overlay
- Example Widget Tests
- Example TextField Overlay
- Example State & Widgets
- Android Gradle Wrapper Scripts
- Example Android MainActivity
- Android Overlay Constants
- MethodChannel
- main.dart
- overlay_dispatcher.dart
- dynamic_island_overlay.dart

## God Nodes (most connected - your core abstractions)
1. `OverlayService` - 36 edges
2. `FlutterOverlayWindowPlugin` - 21 edges
3. `TrayAnimationTimerTask` - 7 edges
4. `🎨 Showcase & Real-World Examples` - 6 edges
5. `WindowSetup` - 5 edges
6. `2.0.0` - 5 edges
7. `1.0.0` - 5 edges
8. `🔄 Bidirectional Communication` - 4 edges
9. `📚 API Reference` - 4 edges
10. `HomePage` - 3 edges

## Surprising Connections (you probably didn't know these)
- `DynamicIslandOverlay` --inherits--> `StatefulWidget`  [EXTRACTED]
  example/lib/overlays/dynamic_island_overlay.dart → None  _Bridges community 4 → community 24_
- `QuickNoteOverlay` --inherits--> `StatefulWidget`  [EXTRACTED]
  example/lib/overlays/quick_note_overlay.dart → None  _Bridges community 4 → community 23_

## Import Cycles
- None detected.

## Communities (25 total, 6 thin omitted)

### Community 0 - "Android Overlay Service"
Cohesion: 0.08
Nodes (20): getCurrentPosition(), BasicMessageChannel, Intent, MethodChannel, moveOverlay(), OverlayService, TrayAnimationTimerTask, updateNotification() (+12 more)

### Community 1 - "Android Plugin Lifecycle & Activity"
Cohesion: 0.14
Nodes (11): Activity, ActivityAware, ActivityPluginBinding, FlutterOverlayWindowPlugin, BasicMessageChannel, Intent, MethodChannel, Context (+3 more)

### Community 2 - "MethodChannel & Notifications Handler"
Cohesion: 0.07
Nodes (26): 0.0.1, 0.0.2, 0.2.1, 0.2.2, 0.2.7, 0.2.8, 0.2.9, 0.3.1 (+18 more)

### Community 3 - "Flutter Plugin Dart API"
Cohesion: 0.07
Nodes (26): dart:async, flutter_overlay_window, _channel, closeOverlay, _controller, disposeOverlayListener, FlutterOverlayWindow, getOverlayPosition (+18 more)

### Community 4 - "Example Home Page & Isolate Port"
Cohesion: 0.05
Nodes (42): dart:developer, HomePage, _HomePageState, build, _buildAvatarBubble, _buildExpandedChat, createState, dispose (+34 more)

### Community 6 - "Overlay Configuration & Alignment"
Cohesion: 0.15
Nodes (12): center,
  centerRight,
  bottomLeft,
  bottomCenter,, bottomRight, fullCover, matchParent, NotificationVisibility, OverlayAlignment, OverlayFlag, PositionGravity (+4 more)

### Community 7 - "Example Entry Point & App Lifecycle"
Cohesion: 0.12
Nodes (15): _activeDesignName, _activityLogs, build, _buildControlsSection, _buildDesignCard, _buildLogsView, _buildStatusHeader, _checkStatus (+7 more)

### Community 8 - "Overlay Position Models"
Cohesion: 0.22
Nodes (8): @immutable, fromMap, OverlayPosition, toMap, toString, x, y, package:flutter/foundation.dart

### Community 9 - "Example TrueCaller Style Overlay"
Cohesion: 0.07
Nodes (26): 1. Centralized Multi-Overlay Router (`OverlayDispatcher`), 1. Declare Permissions, 2. Messenger Chat-Head (Draggable Bubble with Expandable Card), 3. Dynamic Island Capsule (Live Activity Media Player), 4. Quick Note Pad with Keyboard Input (`OverlayFlag.focusPointer`), 5. VIP Caller HUD Banner, 🛠️ Android Configuration (Android 14+ / API 34+ Ready), 📚 API Reference (+18 more)

### Community 13 - "Android Gradle Wrapper Scripts"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 14 - "Example Android MainActivity"
Cohesion: 0.60
Nodes (3): MainActivity, MainActivity, FlutterActivity

### Community 22 - "main.dart"
Cohesion: 0.22
Nodes (8): @pragma, build, main, MyApp, overlayMain, package:flutter_overlay_window_example/home_page.dart, package:flutter_overlay_window_example/overlays/overlay_dispatcher.dart, StatelessWidget

### Community 23 - "overlay_dispatcher.dart"
Cohesion: 0.09
Nodes (23): ByteData?, Color, build, _controller, createState, dispose, _noteColors, QuickNoteOverlay (+15 more)

### Community 24 - "dynamic_island_overlay.dart"
Cohesion: 0.20
Nodes (10): build, _buildCollapsedPill, _buildExpandedView, createState, DynamicIslandOverlay, _DynamicIslandOverlayState, _isExpanded, _isPlaying (+2 more)

## Knowledge Gaps
- **142 isolated node(s):** `OverlayConstants`, `_isOverlayActive`, `_hasPermission`, `_activeDesignName`, `_activityLogs` (+137 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `OverlayConstants`, `_isOverlayActive`, `_hasPermission` to the rest of the system?**
  _142 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Android Overlay Service` be split into smaller, more focused modules?**
  _Cohesion score 0.08333333333333333 - nodes in this community are weakly interconnected._
- **Should `Android Plugin Lifecycle & Activity` be split into smaller, more focused modules?**
  _Cohesion score 0.14130434782608695 - nodes in this community are weakly interconnected._
- **Should `MethodChannel & Notifications Handler` be split into smaller, more focused modules?**
  _Cohesion score 0.07407407407407407 - nodes in this community are weakly interconnected._
- **Should `Flutter Plugin Dart API` be split into smaller, more focused modules?**
  _Cohesion score 0.07407407407407407 - nodes in this community are weakly interconnected._
- **Should `Example Home Page & Isolate Port` be split into smaller, more focused modules?**
  _Cohesion score 0.0545790934320074 - nodes in this community are weakly interconnected._
- **Should `Example Entry Point & App Lifecycle` be split into smaller, more focused modules?**
  _Cohesion score 0.125 - nodes in this community are weakly interconnected._