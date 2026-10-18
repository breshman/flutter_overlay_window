import 'dart:async';
import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool _isOverlayActive = false;
  bool _hasPermission = false;
  String _activeDesignName = "None";
  bool _dismissEnabled = true;
  String _dismissPreset = "red"; // "red", "cyan", "classic", "custom_icon"
  final List<String> _activityLogs = [];
  StreamSubscription<dynamic>? _overlaySubscription;

  @override
  void initState() {
    super.initState();
    _checkStatus();
    _overlaySubscription = FlutterOverlayWindow.overlayListener.listen((data) {
      log("Main app received from overlay: $data");
      if (!mounted) return;
      final timeStr = TimeOfDay.now().format(context);
      setState(() {
        _activityLogs.insert(0, "[$timeStr] $data");
        if (_activityLogs.length > 20) _activityLogs.removeLast();
      });
    });
  }

  @override
  void dispose() {
    _overlaySubscription?.cancel();
    super.dispose();
  }

  Future<void> _checkStatus() async {
    final hasPerm = await FlutterOverlayWindow.isPermissionGranted();
    final isActive = await FlutterOverlayWindow.isActive();
    setState(() {
      _hasPermission = hasPerm;
      _isOverlayActive = isActive;
    });
  }

  Future<bool> _ensurePermission() async {
    final granted = await FlutterOverlayWindow.isPermissionGranted();
    if (!granted) {
      final req = await FlutterOverlayWindow.requestPermission();
      setState(() => _hasPermission = req == true);
      return req == true;
    }
    setState(() => _hasPermission = true);
    return true;
  }

  Future<void> _launchDesign({
    required String designId,
    required String name,
    required int width,
    required int height,
    bool enableDrag = true,
    bool? enableDismiss,
    dynamic dismissIconColor,
    dynamic dismissBackgroundColor,
    String? dismissIcon,
    PositionGravity positionGravity = PositionGravity.auto,
    OverlayFlag flag = OverlayFlag.defaultFlag,
    String? notificationIcon,
  }) async {
    final perm = await _ensurePermission();
    if (!perm) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Overlay permission is required!")),
      );
      return;
    }

    final wasActive = await FlutterOverlayWindow.isActive();

    // Directly call showOverlay; OverlayService cleanly overrides any active window layout
    await FlutterOverlayWindow.showOverlay(
      width: width,
      height: height,
      alignment: OverlayAlignment.center,
      enableDrag: enableDrag,
      enableDismiss: enableDismiss ?? _dismissEnabled,
      dismissIconColor: dismissIconColor,
      dismissBackgroundColor: dismissBackgroundColor,
      dismissIcon: dismissIcon,
      positionGravity: positionGravity,
      flag: flag,
      overlayTitle: "Active: $name",
      overlayContent: "Tap to interact or drag to bottom to dismiss",
      visibility: NotificationVisibility.visibilityPublic,
      notificationIcon: notificationIcon,
    );

    await Future.delayed(const Duration(milliseconds: 150));
    await FlutterOverlayWindow.shareData({"design": designId});

    setState(() {
      _isOverlayActive = true;
      _activeDesignName = name;
      _activityLogs.insert(
        0,
        "[${TimeOfDay.now().format(context)}] ${wasActive ? 'Overrode' : 'Launched'} overlay '$name'",
      );
    });
  }

  void _launchDismissTestOverlay() {
    Color? iconColor;
    Color? bgColor;
    String? customIcon;

    switch (_dismissPreset) {
      case "red":
        bgColor = const Color(0xCCE53935);
        iconColor = Colors.white;
        break;
      case "cyan":
        bgColor = const Color(0xCC002B36);
        iconColor = const Color(0xFF00E5FF);
        break;
      case "custom_icon":
        customIcon = "ic_launcher"; // uses android/app/src/main/res mipmap icon
        break;
      case "classic":
      default:
        // Built-in defaults: translucent dark grey circle with white X
        break;
    }

    _launchDesign(
      designId: "drag_to_dismiss",
      name: "Drag to Dismiss Test",
      width: 240,
      height: 120,
      enableDrag: true,
      enableDismiss: _dismissEnabled,
      dismissIconColor: iconColor,
      dismissBackgroundColor: bgColor,
      dismissIcon: customIcon,
      positionGravity: PositionGravity.auto,
    );
  }

  Future<void> _closeOverlay() async {
    if (await FlutterOverlayWindow.isActive()) {
      await FlutterOverlayWindow.closeOverlay();
    }
    setState(() {
      _isOverlayActive = false;
      _activeDesignName = "None";
      _activityLogs.insert(
        0,
        "[${TimeOfDay.now().format(context)}] Overlay closed",
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text("Overlay Showcase"),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _checkStatus,
            tooltip: "Check Status",
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _buildStatusHeader(theme),
          const SizedBox(height: 16),
          _buildDismissTestingCard(theme),
          const SizedBox(height: 16),
          const Text(
            "Overlay Designs",
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          _buildDesignCard(
            title: "Messenger Chat Head",
            subtitle:
                "Expandable floating bubble with unread badge & chat card",
            badge: "Drag-to-Dismiss Ready",
            icon: Icons.chat_bubble_rounded,
            color: const Color(0xFF0072FF),
            onLaunch: () => _launchDesign(
              designId: "chathead",
              name: "Chat Head",
              width: 80,
              height: 80,
              enableDrag: true,
              enableDismiss: _dismissEnabled,
              dismissBackgroundColor: const Color(0xCCE53935),
              dismissIconColor: Colors.white,
              positionGravity: PositionGravity.auto,
            ),
          ),
          _buildDesignCard(
            title: "Dynamic Island Capsule",
            subtitle:
                "Live activity style media player with progress bar & controls",
            badge: "Compact Pill",
            icon: Icons.play_circle_fill_rounded,
            color: const Color(0xFFE94057),
            onLaunch: () => _launchDesign(
              designId: "dynamic_island",
              name: "Dynamic Island",
              width: 200,
              height: 56,
              enableDrag: true,
              positionGravity: PositionGravity.auto,
            ),
          ),
          _buildDesignCard(
            title: "Quick Note Pad",
            subtitle:
                "Floating sticky note with soft keyboard support & color tags",
            badge: "Keyboard Focusable",
            icon: Icons.edit_note_rounded,
            color: const Color(0xFFF59E0B),
            onLaunch: () => _launchDesign(
              designId: "quick_note",
              name: "Quick Note",
              width: 320,
              height: 290,
              enableDrag: true,
              flag: OverlayFlag.focusPointer,
            ),
          ),
          _buildDesignCard(
            title: "VIP Incoming Call HUD",
            subtitle:
                "Frosted glass caller banner with priority tag & quick response buttons",
            badge: "Interactive Card",
            icon: Icons.phone_in_talk_rounded,
            color: const Color(0xFF10B981),
            onLaunch: () => _launchDesign(
              designId: "true_caller",
              name: "Caller Banner",
              width: 340,
              height: 180,
              enableDrag: true,
              positionGravity: PositionGravity.auto,
            ),
          ),
          const SizedBox(height: 20),
          const Text(
            "Live Controls & Testing",
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          _buildControlsSection(theme),
          const SizedBox(height: 20),
          const Text(
            "Activity Log (Bidirectional)",
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          _buildLogsView(),
        ],
      ),
    );
  }

  Widget _buildStatusHeader(ThemeData theme) {
    return Card(
      elevation: 0,
      color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.4),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: _isOverlayActive
                    ? Colors.green.withValues(alpha: 0.15)
                    : Colors.grey.withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(
                _isOverlayActive
                    ? Icons.layers_rounded
                    : Icons.layers_clear_rounded,
                color: _isOverlayActive ? Colors.green : Colors.grey,
                size: 28,
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    _isOverlayActive
                        ? "Active: $_activeDesignName"
                        : "No Active Overlay",
                    style: const TextStyle(
                        fontWeight: FontWeight.bold, fontSize: 16),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    _hasPermission
                        ? "Permission: Granted"
                        : "Permission: Not Granted",
                    style: TextStyle(
                      fontSize: 12,
                      color: _hasPermission ? Colors.green : Colors.orange,
                    ),
                  ),
                ],
              ),
            ),
            if (_isOverlayActive)
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.redAccent.withValues(alpha: 0.15),
                  foregroundColor: Colors.redAccent,
                  elevation: 0,
                ),
                icon: const Icon(Icons.stop, size: 18),
                label: const Text("Close"),
                onPressed: _closeOverlay,
              )
            else if (!_hasPermission)
              ElevatedButton.icon(
                icon: const Icon(Icons.lock_open, size: 18),
                label: const Text("Allow"),
                onPressed: _ensurePermission,
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildDismissTestingCard(ThemeData theme) {
    return Card(
      elevation: 0,
      color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.35),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.redAccent.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Icon(
                    Icons.swipe_down_rounded,
                    color: Colors.redAccent,
                    size: 22,
                  ),
                ),
                const SizedBox(width: 12),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        "Drag-to-Dismiss Zone",
                        style: TextStyle(
                            fontWeight: FontWeight.bold, fontSize: 15),
                      ),
                      Text(
                        "Shows a dismiss target at bottom-center during drag",
                        style: TextStyle(fontSize: 12, color: Colors.grey),
                      ),
                    ],
                  ),
                ),
                Switch(
                  value: _dismissEnabled,
                  onChanged: (val) => setState(() => _dismissEnabled = val),
                ),
              ],
            ),
            const Divider(height: 24),
            const Text(
              "Zone Style & Color Preset:",
              style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              runSpacing: 6,
              children: [
                ChoiceChip(
                  label: const Text("Red Danger (0xCCE53935)"),
                  selected: _dismissPreset == "red",
                  onSelected: (selected) {
                    if (selected) setState(() => _dismissPreset = "red");
                  },
                ),
                ChoiceChip(
                  label: const Text("Neon Cyan (0xFF00E5FF)"),
                  selected: _dismissPreset == "cyan",
                  onSelected: (selected) {
                    if (selected) setState(() => _dismissPreset = "cyan");
                  },
                ),
                ChoiceChip(
                  label: const Text("Classic (Default)"),
                  selected: _dismissPreset == "classic",
                  onSelected: (selected) {
                    if (selected) setState(() => _dismissPreset = "classic");
                  },
                ),
                ChoiceChip(
                  label: const Text("Custom Icon (ic_launcher)"),
                  selected: _dismissPreset == "custom_icon",
                  onSelected: (selected) {
                    if (selected) {
                      setState(() => _dismissPreset = "custom_icon");
                    }
                  },
                ),
              ],
            ),
            const SizedBox(height: 14),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF4A00E0),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                icon: const Icon(Icons.play_arrow_rounded, size: 20),
                label: const Text(
                  "Launch Drag-to-Dismiss Test Overlay",
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                onPressed: _launchDismissTestOverlay,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDesignCard({
    required String title,
    required String subtitle,
    required String badge,
    required IconData icon,
    required Color color,
    required VoidCallback onLaunch,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onLaunch,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                width: 46,
                height: 46,
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(icon, color: color, size: 26),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Flexible(
                          child: Text(
                            title,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 15,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: color.withValues(alpha: 0.12),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            badge,
                            style: TextStyle(
                              fontSize: 10,
                              fontWeight: FontWeight.w600,
                              color: color,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      subtitle,
                      style: const TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                  ],
                ),
              ),
              const Icon(Icons.arrow_forward_ios, size: 14, color: Colors.grey),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildControlsSection(ThemeData theme) {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        ActionChip(
          avatar: const Icon(Icons.send_rounded, size: 16),
          label: const Text("Send Ping to Overlay"),
          onPressed: () async {
            await FlutterOverlayWindow.shareData(
              "Ping from App at ${TimeOfDay.now().format(context)}",
            );
          },
        ),
        ActionChip(
          avatar: const Icon(Icons.notifications_active, size: 16),
          label: const Text("Update Notification"),
          onPressed: () async {
            await FlutterOverlayWindow.updateNotification(
              title: "Showcase Notification",
              content: "Synced at ${TimeOfDay.now().format(context)}",
            );
          },
        ),
        ActionChip(
          avatar: const Icon(Icons.my_location, size: 16),
          label: const Text("Get Position"),
          onPressed: () async {
            final pos = await FlutterOverlayWindow.getOverlayPosition();
            if (!context.mounted) return;
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text("Overlay Position: $pos")),
            );
          },
        ),
        ActionChip(
          avatar: const Icon(Icons.open_with_rounded, size: 16),
          label: const Text("Move (100, 200)"),
          onPressed: () async {
            await FlutterOverlayWindow.moveOverlay(
              const OverlayPosition(100, 200),
            );
            if (!context.mounted) return;
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text("Moved overlay to (100, 200)")),
            );
          },
        ),
        ActionChip(
          avatar: const Icon(Icons.close_rounded, size: 16),
          label: const Text("Stop Overlay"),
          onPressed: _closeOverlay,
        ),
      ],
    );
  }

  Widget _buildLogsView() {
    if (_activityLogs.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Colors.grey.withValues(alpha: 0.08),
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Center(
          child: Text(
            "No messages yet. Interact with any overlay to see events here.",
            style: TextStyle(color: Colors.grey, fontSize: 13),
          ),
        ),
      );
    }

    return Container(
      height: 160,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.black.withValues(alpha: 0.05),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.black12),
      ),
      child: ListView.builder(
        itemCount: _activityLogs.length,
        itemBuilder: (context, index) {
          return Padding(
            padding: const EdgeInsets.symmetric(vertical: 2),
            child: Text(
              _activityLogs[index],
              style: const TextStyle(fontSize: 12, fontFamily: 'monospace'),
            ),
          );
        },
      ),
    );
  }
}
