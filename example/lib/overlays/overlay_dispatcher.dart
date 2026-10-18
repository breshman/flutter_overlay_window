import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';
import 'package:flutter_overlay_window_example/overlays/dynamic_island_overlay.dart';
import 'package:flutter_overlay_window_example/overlays/messanger_chathead.dart';
import 'package:flutter_overlay_window_example/overlays/quick_note_overlay.dart';
import 'package:flutter_overlay_window_example/overlays/true_caller_overlay.dart';

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
      log("OverlayDispatcher received: $data");
      if (data is Map && data.containsKey("design")) {
        setState(() {
          _activeDesign = data["design"] as String;
        });
      } else if (data is String) {
        if (["chathead", "dynamic_island", "quick_note", "true_caller"].contains(data)) {
          setState(() {
            _activeDesign = data;
          });
        }
      }
    });
  }

  Widget _buildSelectedOverlay() {
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

  @override
  Widget build(BuildContext context) {
    return _buildSelectedOverlay();
  }
}
