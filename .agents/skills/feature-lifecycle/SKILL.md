---
name: feature-lifecycle
description: >-
  Use this skill whenever adding new features, refactoring existing functionality,
  or writing tests in flutter_overlay_window. Enforces the dual test verification gate
  (Flutter and Kotlin), architecture patterns, lifecycle decoupling, and documentation standards.
---

# Feature Lifecycle & Quality Assurance Skill

This skill guides you through the full development cycle of implementing features, refactoring, or extending the `flutter_overlay_window` plugin.

---

## 📋 The 5-Phase Implementation Process

When adding a new feature or modifying functionality, follow these phases sequentially:

### Phase 1: Dart Interface Design
1. Open [`lib/src/overlay_window.dart`](../../../lib/src/overlay_window.dart).
2. Add strongly-typed method parameters or new methods.
3. If accepting color values, support both Flutter `Color` and ARGB `int` using `.toARGB32()`.
4. Document all parameters with DartDoc comments.
5. If new models or enums are needed, place them in [`lib/src/models/`](../../../lib/src/models/) with `toMap()` and `fromMap()` methods.

### Phase 2: Android Native Implementation (100% Kotlin)
1. **Declare Constants**: Update [`OverlayConstants.kt`](../../../android/src/main/kotlin/flutter/overlay/window/flutter_overlay_window/OverlayConstants.kt) with new keys or defaults.
2. **Setup Model**: Update [`WindowSetup.kt`](../../../android/src/main/kotlin/flutter/overlay/window/flutter_overlay_window/WindowSetup.kt) with fields and mapping functions.
3. **Plugin Layer**: Update [`FlutterOverlayWindowPlugin.kt`](../../../android/src/main/kotlin/flutter/overlay/window/flutter_overlay_window/FlutterOverlayWindowPlugin.kt):
   - Extract arguments from `MethodCall`.
   - Put parameters into `Intent.extras` to decouple lifecycle.
4. **Service Layer**: Update [`OverlayService.kt`](../../../android/src/main/kotlin/flutter/overlay/window/flutter_overlay_window/OverlayService.kt):
   - Extract configuration from `intent.extras`.
   - Implement touch/window management without killing the background service.
   - Clean up views in `removeCurrentOverlayView()` and `onDestroy()`.
   - Recalculate metrics in `onConfigurationChanged()`.

### Phase 3: Writing Tests (Dual Gate)
Every feature MUST include tests on both sides:

1. **Dart Unit Tests**:
   - Add test cases in [`test/flutter_overlay_window_test.dart`](../../../test/flutter_overlay_window_test.dart) or [`test/overlay_models_test.dart`](../../../test/overlay_models_test.dart).
   - Verify method channel arguments and message channel payloads.

2. **Kotlin Unit Tests**:
   - Add test cases in [`android/src/test/kotlin/flutter/overlay/window/flutter_overlay_window/`](../../../android/src/test/kotlin/flutter/overlay/window/flutter_overlay_window/).
   - Test mappings, flags, gravity calculations, and defaults using JUnit 4.

### Phase 4: Example App Integration
1. Create a demonstration overlay widget in [`example/lib/overlays/`](../../../example/lib/overlays/).
2. Register the design ID in [`OverlayDispatcher`](../../../example/lib/overlays/overlay_dispatcher.dart).
3. Add a launcher card or testing controls in [`HomePage`](../../../example/lib/home_page.dart).

### Phase 5: Verification & Quality Gate
Run the automated verification script:

```bash
.agents/skills/feature-lifecycle/scripts/run_all_tests.sh
```

Or execute manually:
1. `flutter analyze` (Must report 0 issues).
2. `cd example && flutter analyze` (Must report 0 issues).
3. `flutter test` (Must pass 100%).
4. `cd example/android && ./gradlew :flutter_overlay_window:testDebugUnitTest` (Must pass 100%).
5. `graphify update .` (Keep knowledge graph synchronized).

---

## 📝 Documentation Checklist

Before completing your turn:
- [ ] Update `CHANGELOG.md` with features, bug fixes, or architecture updates.
- [ ] Update `README.md` with:
  - Total test pass count in badge.
  - Parameter description in API Reference.
  - Code snippet demonstration.
