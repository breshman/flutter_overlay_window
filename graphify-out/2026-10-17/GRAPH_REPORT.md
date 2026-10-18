# Graph Report - flutter_overlay_window  (2026-10-17)

## Corpus Check
- 33 files · ~11,122 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 292 nodes · 373 edges · 25 communities (19 shown, 6 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `40a31fa0`
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
1. `OverlayService` - 34 edges
2. `FlutterOverlayWindowPlugin` - 21 edges
3. `TrayAnimationTimerTask` - 7 edges
4. `WindowSetup` - 5 edges
5. `1.0.0` - 5 edges
6. `Real-World Examples` - 5 edges
7. `API Reference` - 4 edges
8. `HomePage` - 3 edges
9. `_HomePageState` - 3 edges
10. `DynamicIslandOverlay` - 3 edges

## Surprising Connections (you probably didn't know these)
- `DynamicIslandOverlay` --inherits--> `StatefulWidget`  [EXTRACTED]
  example/lib/overlays/dynamic_island_overlay.dart → None  _Bridges community 7 → community 24_
- `OverlayDispatcher` --inherits--> `StatefulWidget`  [EXTRACTED]
  example/lib/overlays/overlay_dispatcher.dart → None  _Bridges community 7 → community 23_

## Import Cycles
- None detected.

## Communities (25 total, 6 thin omitted)

### Community 0 - "Android Overlay Service"
Cohesion: 0.09
Nodes (19): getCurrentPosition(), BasicMessageChannel, Intent, MethodChannel, moveOverlay(), OverlayService, TrayAnimationTimerTask, updateNotification() (+11 more)

### Community 1 - "Android Plugin Lifecycle & Activity"
Cohesion: 0.14
Nodes (11): Activity, ActivityAware, ActivityPluginBinding, FlutterOverlayWindowPlugin, BasicMessageChannel, Intent, MethodChannel, Context (+3 more)

### Community 2 - "MethodChannel & Notifications Handler"
Cohesion: 0.09
Nodes (21): 0.0.1, 0.0.2, 0.2.1, 0.2.2, 0.2.7, 0.2.8, 0.2.9, 0.3.1 (+13 more)

### Community 3 - "Flutter Plugin Dart API"
Cohesion: 0.07
Nodes (26): dart:async, flutter_overlay_window, _channel, closeOverlay, _controller, disposeOverlayListener, FlutterOverlayWindow, getOverlayPosition (+18 more)

### Community 4 - "Example Home Page & Isolate Port"
Cohesion: 0.12
Nodes (15): _activeDesignName, _activityLogs, build, _buildControlsSection, _buildDesignCard, _buildLogsView, _buildStatusHeader, _checkStatus (+7 more)

### Community 6 - "Overlay Configuration & Alignment"
Cohesion: 0.15
Nodes (12): center,
  centerRight,
  bottomLeft,
  bottomCenter,, bottomRight, fullCover, matchParent, NotificationVisibility, OverlayAlignment, OverlayFlag, PositionGravity (+4 more)

### Community 7 - "Example Entry Point & App Lifecycle"
Cohesion: 0.06
Nodes (40): Color, dart:developer, HomePage, _HomePageState, build, _buildAvatarBubble, _buildExpandedChat, createState (+32 more)

### Community 8 - "Overlay Position Models"
Cohesion: 0.14
Nodes (12): @immutable, fromMap, OverlayPosition, toMap, toString, x, y, package:flutter/foundation.dart (+4 more)

### Community 9 - "Example TrueCaller Style Overlay"
Cohesion: 0.11
Nodes (17): 1. Basic Flow: Requesting Permissions and Showing the Overlay, 2. Messenger-Style Chat Head (Expandable & Draggable), 3. Bidirectional Communication (Main App $\leftrightarrow$ Overlay), 4. Interactive Overlay with Keyboard / Text Fields, Android Configuration (Android 14+ / API 34+ Ready), API Reference, Architecture & Entry Point, Defining the Entry Point in `lib/main.dart` (+9 more)

### Community 13 - "Android Gradle Wrapper Scripts"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 14 - "Example Android MainActivity"
Cohesion: 0.60
Nodes (3): MainActivity, MainActivity, FlutterActivity

### Community 22 - "main.dart"
Cohesion: 0.15
Nodes (11): @pragma, build, main, MyApp, overlayMain, main, package:flutter/material.dart, package:flutter_overlay_window_example/home_page.dart (+3 more)

### Community 23 - "overlay_dispatcher.dart"
Cohesion: 0.18
Nodes (11): _activeDesign, build, _buildSelectedOverlay, createState, initState, OverlayDispatcher, _OverlayDispatcherState, package:flutter_overlay_window_example/overlays/dynamic_island_overlay.dart (+3 more)

### Community 24 - "dynamic_island_overlay.dart"
Cohesion: 0.20
Nodes (10): build, _buildCollapsedPill, _buildExpandedView, createState, DynamicIslandOverlay, _DynamicIslandOverlayState, _isExpanded, _isPlaying (+2 more)

## Knowledge Gaps
- **125 isolated node(s):** `OverlayConstants`, `_isOverlayActive`, `_hasPermission`, `_activeDesignName`, `_activityLogs` (+120 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `OverlayConstants`, `_isOverlayActive`, `_hasPermission` to the rest of the system?**
  _125 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Android Overlay Service` be split into smaller, more focused modules?**
  _Cohesion score 0.08792270531400966 - nodes in this community are weakly interconnected._
- **Should `Android Plugin Lifecycle & Activity` be split into smaller, more focused modules?**
  _Cohesion score 0.14130434782608695 - nodes in this community are weakly interconnected._
- **Should `MethodChannel & Notifications Handler` be split into smaller, more focused modules?**
  _Cohesion score 0.09090909090909091 - nodes in this community are weakly interconnected._
- **Should `Flutter Plugin Dart API` be split into smaller, more focused modules?**
  _Cohesion score 0.07407407407407407 - nodes in this community are weakly interconnected._
- **Should `Example Home Page & Isolate Port` be split into smaller, more focused modules?**
  _Cohesion score 0.125 - nodes in this community are weakly interconnected._
- **Should `Example Entry Point & App Lifecycle` be split into smaller, more focused modules?**
  _Cohesion score 0.06025369978858351 - nodes in this community are weakly interconnected._