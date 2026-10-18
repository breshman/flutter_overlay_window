## 2.0.0

### Kotlin Migration & Architecture
- Complete migration of the Android native layer from Java to 100% Kotlin (`FlutterOverlayWindowPlugin.kt`, `OverlayService.kt`, `WindowSetup.kt`, `OverlayConstants.kt`).
- Adopted the Flutter Built-in Kotlin standard (KGP removed from plugin `build.gradle`), ensuring out-of-the-box compatibility with modern Gradle 8/9 and AGP 8.x/9.x without conflicting with host applications.
- Unified compiler target to JVM 17 (`compilerOptions.jvmTarget.set(JvmTarget.JVM_17)`).

### Android 14+ (API 34/35) & Modern Android Support
- Full support for Android 14+ Foreground Service requirements using `ServiceCompat.startForeground` with `FOREGROUND_SERVICE_TYPE_SPECIAL_USE`.
- Declared permissions for `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` and `android.permission.POST_NOTIFICATIONS` (Android 13+).
- Removed deprecated `package` attribute from `AndroidManifest.xml` (governed via `namespace` DSL in `build.gradle`).
- Removed hardcoded AGP dependencies and `rootProject.allprojects` from the plugin's `build.gradle`, resolving repository conflicts (`FAIL_ON_PROJECT_REPOS`) and AGP version collisions when consumed by other apps.

### Dart & Flutter SDK Updates
- Raised minimum SDK environment constraints to Dart `>=3.0.0 <4.0.0` and Flutter `>=3.16.0`.
- Upgraded `flutter_lints` to `^4.0.0`.
- Modernized unit tests with `TestDefaultBinaryMessengerBinding`.

### Example App & Showcase Overlays
- Upgraded example app to modern Flutter 3.x and Dart 3.x with Material 3 design system.
- Completely redesigned `HomePage` into an interactive showcase dashboard with live status indicators, permission controls, and bidirectional log viewer.
- Added 4 brand-new interactive overlay designs:
  - **Dynamic Island Capsule**: Live activity style compact pill with expand-to-full media player card and progress slider.
  - **Messenger Chat Head**: Sleek floating circular bubble with unread badge, drag-to-snap physics, and expandable chat card with quick replies.
  - **Quick Note Pad**: Floating sticky note with soft keyboard support using `OverlayFlag.focusPointer` and color palette tags.
  - **VIP Caller HUD**: Frosted glass caller banner with priority tag, contact info, and Accept/Decline action buttons.
- Added centralized `OverlayDispatcher` to dynamically switch between overlay designs via `overlayListener`.

## 1.0.0

### Features
- Add `notificationIcon` to `showOverlay`: use any drawable or mipmap resource from your app as the notification icon (#173, #174)
- Add `updateNotification`: change the notification title, content or icon while the overlay is showing
- Overlay now respects the status and navigation bars and stays within the visible screen

### Fixes
- Fix `requestPermission()` never completing after the user grants permission (#187)
- Fix `requestPermission()` hanging after a screen rotation while the settings page is open
- Fix `requestPermission()` returning `true` without asking on Android 6 and 7
- `requestPermission()` now returns `true` immediately when permission is already granted
- `requestPermission()` returns a `NO_ACTIVITY` error instead of crashing when no activity is attached
- Fix other method calls overwriting a pending `requestPermission()` result
- Fix crash when the permission result arrives with no pending request
- Fix overlay drag issues and smoother snap animation
- Fix position calculation for centered overlays
- A missing notification icon falls back to the launcher icon instead of crashing the service

### Build
- Upgrade to Android Gradle Plugin 9 and Gradle 9.1
- Java 17 is now required

### Example
- Add buttons to test the custom notification icon and `updateNotification`

## 0.5.0
- Update gradle version
- Fix `NullPointerException` in OverlayService


## 0.4.5
- Added instructions for android 14 compatibility

## 0.4.4
- Fix overlay close crash  
- Add `startPosition`: start overlay in default position  
- Add `moveOverlay`: Update the overlay position in the screen  
- Add `getOverlayPosition`: Get the current overlay position

## 0.4.3
- Fix overlay height bug

## 0.4.2
- Fix touch freeze

## 0.4.1
- Remove secure flag  
- Detach view from engine after closing  
- Fix Example to show (Sending data between Main & overlay)

## 0.3.3
- Fix bugs related to android 12+  
- Some code optimizations  
- Fix overlay popup on top of status bar  
- Fix overlay closing

## 0.3.2
- Add the position gravity feature

## 0.3.1
- Fix the overlay permission on Android versions <= 6  
- Add the possibility to resize overlay while it's in action

## 0.2.9
- Fix closing overlay  
- Possibility to check if the overlay is active or not

## 0.2.8
- Change overlay flags names

## 0.2.7
- Fix overlay issue to target all SDK versions  
- Add `overlayTitle` and `overlayContent` arguments  
- Fix typo

## 0.2.2
- Add custom notification content text  
- Improve the `flagNotFocusable`  
- Update example

## 0.2.1
- Add flag update on runtime

## 0.0.2
- Fix keyboard not showing on TextFields

## 0.0.1
- Initial release
