# Graph Report - flutter_overlay_window  (2026-10-18)

## Corpus Check
- 67 files · ~23,805 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 526 nodes · 620 edges · 51 communities (39 shown, 12 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d33a6815`
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
- dynamic_island_overlay.dart
- OverlayConstantsTest
- flutter_overlay_window_test.dart
- quick_note_overlay.dart
- State
- true_caller_overlay.dart
- Flutter Package Development
- Integration strategy
- Determinism, goldens, and integration
- Find Skills
- package:flutter/material.dart
- Android Kotlin Core
- Android Testing Unit
- State
- text_field_overlay.dart
- Android Kotlin Core Patterns
- Android Kotlin Core Runnable Scenarios
- Android Testing Unit Patterns
- Android Testing Unit Runnable Scenarios
- official-links.md
- run_examples.sh
- official-links.md
- run_examples.sh
- messanger_chathead.dart
- package:flutter/material.dart

## God Nodes (most connected - your core abstractions)
1. `OverlayService` - 42 edges
2. `FlutterOverlayWindowPlugin` - 21 edges
3. `WindowSetupTest` - 10 edges
4. `DismissDrawable` - 9 edges
5. `Android Kotlin Core` - 8 edges
6. `Android Testing Unit` - 8 edges
7. `TrayAnimationTimerTask` - 7 edges
8. `Find Skills` - 7 edges
9. `How to Help Users Find Skills` - 7 edges
10. `Integration strategy` - 7 edges

## Surprising Connections (you probably didn't know these)
- `WindowSetupTest` --references--> `WindowSetup`  [EXTRACTED]
  android/src/test/kotlin/flutter/overlay/window/flutter_overlay_window/WindowSetupTest.kt → android/src/main/kotlin/flutter/overlay/window/flutter_overlay_window/WindowSetup.kt

## Import Cycles
- None detected.

## Communities (51 total, 12 thin omitted)

### Community 0 - "Android Overlay Service"
Cohesion: 0.08
Nodes (21): getCurrentPosition(), BasicMessageChannel, Intent, MethodChannel, moveOverlay(), OverlayService, TrayAnimationTimerTask, updateNotification() (+13 more)

### Community 1 - "Android Plugin Lifecycle & Activity"
Cohesion: 0.14
Nodes (11): Activity, ActivityAware, ActivityPluginBinding, FlutterOverlayWindowPlugin, BasicMessageChannel, Intent, MethodChannel, Context (+3 more)

### Community 2 - "MethodChannel & Notifications Handler"
Cohesion: 0.07
Nodes (26): 0.0.1, 0.0.2, 0.2.1, 0.2.2, 0.2.7, 0.2.8, 0.2.9, 0.3.1 (+18 more)

### Community 3 - "Flutter Plugin Dart API"
Cohesion: 0.08
Nodes (25): flutter_overlay_window, _channel, closeOverlay, _controller, disposeOverlayListener, FlutterOverlayWindow, getOverlayPosition, isActive (+17 more)

### Community 4 - "Example Home Page & Isolate Port"
Cohesion: 0.09
Nodes (21): _activeDesignName, _activityLogs, build, _buildControlsSection, _buildDesignCard, _buildDismissTestingCard, _buildLogsView, _buildStatusHeader (+13 more)

### Community 5 - "Example Messenger Chathead Overlay"
Cohesion: 0.12
Nodes (3): BasicMessageChannel, WindowSetup, WindowSetupTest

### Community 6 - "Overlay Configuration & Alignment"
Cohesion: 0.15
Nodes (12): center,
  centerRight,
  bottomLeft,
  bottomCenter,, bottomRight, fullCover, matchParent, NotificationVisibility, OverlayAlignment, OverlayFlag, PositionGravity (+4 more)

### Community 7 - "Example Entry Point & App Lifecycle"
Cohesion: 0.15
Nodes (12): _activeDesign, build, _buildSelectedOverlay, createState, dispose, initState, _overlaySubscription, package:flutter_overlay_window_example/overlays/drag_to_dismiss_overlay.dart (+4 more)

### Community 8 - "Overlay Position Models"
Cohesion: 0.22
Nodes (8): @immutable, fromMap, OverlayPosition, toMap, toString, x, y, package:flutter/foundation.dart

### Community 9 - "Example TrueCaller Style Overlay"
Cohesion: 0.07
Nodes (29): 1. Centralized Multi-Overlay Router (`OverlayDispatcher`), 1. Declare Permissions, 1. Flutter & Dart Tests (19 tests), 2. Android Native Unit Tests (11 tests), 2. Messenger Chat-Head (Draggable Bubble with Expandable Card), 3. Dynamic Island Capsule (Live Activity Media Player), 4. Quick Note Pad with Keyboard Input (`OverlayFlag.focusPointer`), 5. VIP Caller HUD Banner (+21 more)

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
Cohesion: 0.08
Nodes (25): ByteData?, Color, build, _controller, createState, dispose, _noteColors, QuickNoteOverlay (+17 more)

### Community 24 - "dynamic_island_overlay.dart"
Cohesion: 0.20
Nodes (4): DismissDrawable, Canvas, ColorFilter, Drawable

### Community 25 - "dynamic_island_overlay.dart"
Cohesion: 0.20
Nodes (10): build, _buildCollapsedPill, _buildExpandedView, createState, DynamicIslandOverlay, _DynamicIslandOverlayState, _isExpanded, _isPlaying (+2 more)

### Community 27 - "flutter_overlay_window_test.dart"
Cohesion: 0.20
Nodes (9): 1. Mandatory Dual Test Passing Gate, 2. Architecture & Design Patterns, 3. Documentation & Example Integrity, A. Intent-Based Configuration (Decoupled Native Lifecycle), Architecture Standards & Quality Gate, B. Orientation & Screen Resilience, C. Seamless View Overrides, D. Reactive Streams & Safe Disposal (+1 more)

### Community 28 - "quick_note_overlay.dart"
Cohesion: 0.22
Nodes (8): 📝 Documentation Checklist, Feature Lifecycle & Quality Assurance Skill, Phase 1: Dart Interface Design, Phase 2: Android Native Implementation (100% Kotlin), Phase 3: Writing Tests (Dual Gate), Phase 4: Example App Integration, Phase 5: Verification & Quality Gate, 📋 The 5-Phase Implementation Process

### Community 31 - "Flutter Package Development"
Cohesion: 0.09
Nodes (18): Federation threshold, Platform evidence, Plugins and federation, Shape the plugin deliberately, Contract first, Dependency discipline, Package structure, Public API and package quality (+10 more)

### Community 32 - "Integration strategy"
Cohesion: 0.09
Nodes (19): Existing package, FFI and native assets, Integration strategy, Platform channels and Pigeon, Platform views, Verification, Web interop, Choose package shape (+11 more)

### Community 33 - "Determinism, goldens, and integration"
Cohesion: 0.11
Nodes (15): Async determinism, Cross-Platform Font Rasterization, Determinism, goldens, and integration, Disposable source and runtime artifacts, Goldens, Integration, Sources, Sources (+7 more)

### Community 34 - "Find Skills"
Cohesion: 0.14
Nodes (13): Common Skill Categories, Find Skills, How to Help Users Find Skills, Step 1: Understand What They Need, Step 2: Check the Leaderboard First, Step 3: Search for Skills, Step 4: Verify Quality Before Recommending, Step 5: Present Options to the User (+5 more)

### Community 35 - "package:flutter/material.dart"
Cohesion: 0.32
Nodes (8): HomePage, _HomePageState, MessangerChatHead, _MessangerChatHeadState, OverlayDispatcher, _OverlayDispatcherState, State, StatefulWidget

### Community 36 - "Android Kotlin Core"
Cohesion: 0.17
Nodes (11): Android Kotlin Core, Anti-Patterns, Done Checklist, Edge case, Examples, Failure recovery, Guardrails, Happy path (+3 more)

### Community 37 - "Android Testing Unit"
Cohesion: 0.17
Nodes (11): Android Testing Unit, Anti-Patterns, Done Checklist, Edge case, Examples, Failure recovery, Guardrails, Happy path (+3 more)

### Community 38 - "State"
Cohesion: 0.25
Nodes (7): build, _counter, createState, DragToDismissOverlay, _DragToDismissOverlayState, package:flutter_overlay_window/flutter_overlay_window.dart, main

### Community 39 - "text_field_overlay.dart"
Cohesion: 0.20
Nodes (10): dart:async, dart:developer, build, createState, dispose, initState, _subscription, TextFieldOverlay (+2 more)

### Community 40 - "Android Kotlin Core Patterns"
Cohesion: 0.40
Nodes (4): Android Kotlin Core Patterns, Default Review Sequence, Handoff Shortlist, Selection Notes

### Community 41 - "Android Kotlin Core Runnable Scenarios"
Cohesion: 0.40
Nodes (4): Android Kotlin Core Runnable Scenarios, Edge case, Failure recovery, Happy path

### Community 42 - "Android Testing Unit Patterns"
Cohesion: 0.40
Nodes (4): Android Testing Unit Patterns, Default Review Sequence, Handoff Shortlist, Selection Notes

### Community 43 - "Android Testing Unit Runnable Scenarios"
Cohesion: 0.40
Nodes (4): Android Testing Unit Runnable Scenarios, Edge case, Failure recovery, Happy path

### Community 49 - "messanger_chathead.dart"
Cohesion: 0.14
Nodes (13): build, _buildAvatarBubble, _buildExpandedChat, createState, dispose, initState, _isExpanded, _messages (+5 more)

### Community 50 - "package:flutter/material.dart"
Cohesion: 0.40
Nodes (5): build, createState, TrueCallerOverlay, _TrueCallerOverlayState, package:flutter/material.dart

## Knowledge Gaps
- **261 isolated node(s):** `run_examples.sh script`, `run_examples.sh script`, `run_all_tests.sh script`, `OverlayConstants`, `_isOverlayActive` (+256 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DismissDrawable` connect `dynamic_island_overlay.dart` to `Android Overlay Service`?**
  _High betweenness centrality (0.004) - this node is a cross-community bridge._
- **What connects `run_examples.sh script`, `run_examples.sh script`, `run_all_tests.sh script` to the rest of the system?**
  _261 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Android Overlay Service` be split into smaller, more focused modules?**
  _Cohesion score 0.08080808080808081 - nodes in this community are weakly interconnected._
- **Should `Android Plugin Lifecycle & Activity` be split into smaller, more focused modules?**
  _Cohesion score 0.14130434782608695 - nodes in this community are weakly interconnected._
- **Should `MethodChannel & Notifications Handler` be split into smaller, more focused modules?**
  _Cohesion score 0.07407407407407407 - nodes in this community are weakly interconnected._
- **Should `Flutter Plugin Dart API` be split into smaller, more focused modules?**
  _Cohesion score 0.07692307692307693 - nodes in this community are weakly interconnected._
- **Should `Example Home Page & Isolate Port` be split into smaller, more focused modules?**
  _Cohesion score 0.09090909090909091 - nodes in this community are weakly interconnected._