---
trigger: always_on
description: Strict architecture standards, design patterns, and mandatory dual test passing (Flutter and Kotlin) for flutter_overlay_window.
---

# Architecture Standards & Quality Gate

Every new feature, refactor, or bugfix in `flutter_overlay_window` MUST strictly adhere to the following architecture patterns, design guidelines, and testing quality gates.

## 1. Mandatory Dual Test Passing Gate

Before declaring any change complete or delivering code to the user, you MUST run and pass BOTH test suites:

1. **Flutter & Dart Tests (100% Passing)**:
   ```bash
   flutter test
   ```
   - Must maintain 0 failures and 0 errors.
   - Any new method, parameter, or model MUST have corresponding test coverage in `test/flutter_overlay_window_test.dart` or `test/overlay_models_test.dart`.

2. **Android Native Kotlin Tests (100% Passing)**:
   ```bash
   cd example/android && ./gradlew :flutter_overlay_window:testDebugUnitTest
   ```
   - Must maintain 0 failures and 0 errors.
   - Any new window flag, alignment, gravity, Intent parameter, or service behavior MUST have corresponding test coverage in `android/src/test/kotlin/...`.

3. **Static Analysis (Zero Warnings)**:
   ```bash
   flutter analyze
   ```
   - Must produce `No issues found!` in both root and `example/`. No deprecated API usages or unused imports.

---

## 2. Architecture & Design Patterns

### A. Intent-Based Configuration (Decoupled Native Lifecycle)
- **Never rely exclusively on memory-bound singletons** (`WindowSetup.instance`) across service starts or recreations.
- All configuration parameters (dimensions, flags, alignment, drag, dismiss, notifications) MUST be serialized into Android `Intent.extras` by `FlutterOverlayWindowPlugin` and read by `OverlayService.onStartCommand`.
- This ensures overlay configuration survives process recreation, configuration changes, and background restarts.

### B. Orientation & Screen Resilience
- Native `OverlayService` must implement `onConfigurationChanged` to dynamically recalculate screen metrics, status bar insets, and safe window boundaries.
- Overlays must stay within screen bounds without crashing or being lost offscreen during Portrait $\leftrightarrow$ Landscape transitions.
- The FlutterEngine instance must be preserved across rotations without tearing down.

### C. Seamless View Overrides
- Calling `showOverlay` while an overlay is already active must cleanly replace the active window layout without terminating the background Foreground Service lifecycle (`cleanupAndStopSelf()` must NOT be called on new launches).
- Use `removeCurrentOverlayView()` to detach the old view and attach the new one smoothly.

### D. Reactive Streams & Safe Disposal
- `FlutterOverlayWindow.overlayListener` MUST use `StreamController<dynamic>.broadcast()` to support multiple concurrent listeners.
- `disposeOverlayListener()` must safely close the active controller and ensure the next call to `overlayListener` transparently resurrects a fresh, open stream controller.
- Bidirectional messaging between the host app and overlay must use `StandardMessageCodec` via `BasicMessageChannel` to support high-performance binary serialization for Maps, Lists, and primitives.

### E. Touch & Window Hierarchy
- Non-focusable overlays must use `FLAG_NOT_FOCUSABLE`.
- Text input / keyboard overlays must use `FLAG_NOT_TOUCH_MODAL` via `OverlayFlag.focusPointer`.
- Click-through overlays must combine `FLAG_NOT_TOUCHABLE`, `FLAG_NOT_FOCUSABLE`, `FLAG_LAYOUT_NO_LIMITS`, and `FLAG_LAYOUT_IN_SCREEN`.
- Dismiss targets must only appear if explicitly enabled (`enableDismiss == true`).

---

## 3. Documentation & Example Integrity

For every added feature or public API modification:
1. Update `lib/src/overlay_window.dart` with comprehensive DartDoc comments.
2. Update `README.md` with:
   - Updated badge counts (tests passing).
   - Real-world working code example.
   - Entry in the API Reference table and parameters list.
3. Update `CHANGELOG.md` under the active version.
4. Add a runnable example in `example/lib/overlays/` and hook it into `example/lib/overlays/overlay_dispatcher.dart` and `example/lib/home_page.dart`.
5. Run `graphify update .` to keep the codebase knowledge graph current.
