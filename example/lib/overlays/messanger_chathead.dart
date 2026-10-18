import 'dart:async';
import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

class MessangerChatHead extends StatefulWidget {
  const MessangerChatHead({super.key});

  @override
  State<MessangerChatHead> createState() => _MessangerChatHeadState();
}

class _MessangerChatHeadState extends State<MessangerChatHead> {
  bool _isExpanded = false;
  final List<String> _messages = [
    "Hey! Are you available to review the latest build?",
    "Everything is running smoothly on Android 14.",
  ];
  final TextEditingController _replyController = TextEditingController();
  StreamSubscription<dynamic>? _overlaySubscription;

  @override
  void initState() {
    super.initState();
    _overlaySubscription = FlutterOverlayWindow.overlayListener.listen((data) {
      log("Overlay received: $data");
      if (!mounted) return;
      if (data is String) {
        setState(() {
          _messages.add(data);
        });
      }
    });
  }

  @override
  void dispose() {
    _overlaySubscription?.cancel();
    _replyController.dispose();
    super.dispose();
  }

  Future<void> _toggleExpand() async {
    if (_isExpanded) {
      await FlutterOverlayWindow.resizeOverlay(96, 96, true);
    } else {
      await FlutterOverlayWindow.resizeOverlay(350, 410, false);
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
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 220),
          curve: Curves.easeInOut,
          width: _isExpanded ? 350 : 96,
          height: _isExpanded ? 410 : 96,
          child: _isExpanded ? _buildExpandedChat() : _buildAvatarBubble(),
        ),
      ),
    );
  }

  Widget _buildAvatarBubble() {
    return GestureDetector(
      onTap: _toggleExpand,
      child: Stack(
        alignment: Alignment.center,
        children: [
          Container(
            width: 68,
            height: 68,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFF0072FF), Color(0xFF00C6FF)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF0072FF).withValues(alpha: 0.4),
                  blurRadius: 14,
                  offset: const Offset(0, 6),
                ),
              ],
            ),
            child: const Icon(
              Icons.forum_rounded,
              color: Colors.white,
              size: 34,
            ),
          ),
          Positioned(
            right: 4,
            top: 4,
            child: Container(
              padding: const EdgeInsets.all(4),
              decoration: const BoxDecoration(
                color: Color(0xFFFF3366),
                shape: BoxShape.circle,
              ),
              child: Text(
                "${_messages.length}",
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 10,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildExpandedChat() {
    return Container(
      margin: const EdgeInsets.all(15),
      decoration: BoxDecoration(
        color: const Color(0xFF1E222D),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.white10),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.5),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        children: [
          // Header
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: const BoxDecoration(
              color: Color(0xFF262C3A),
              borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
            ),
            child: Row(
              children: [
                Container(
                  width: 36,
                  height: 36,
                  decoration: const BoxDecoration(
                    gradient: LinearGradient(
                      colors: [Color(0xFF0072FF), Color(0xFF00C6FF)],
                    ),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.person, color: Colors.white, size: 20),
                ),
                const SizedBox(width: 10),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        "Odoo Mobile Team",
                        style: TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
                      ),
                      Text(
                        "Active now",
                        style: TextStyle(color: Colors.greenAccent, fontSize: 11),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(),
                  icon: const Icon(Icons.remove, color: Colors.white70),
                  onPressed: _toggleExpand,
                ),
                const SizedBox(width: 8),
                IconButton(
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(),
                  icon: const Icon(Icons.close, color: Colors.redAccent),
                  onPressed: () => FlutterOverlayWindow.closeOverlay(),
                ),
              ],
            ),
          ),

          // Messages list
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              itemCount: _messages.length,
              itemBuilder: (context, index) {
                return Align(
                  alignment: Alignment.centerLeft,
                  child: Container(
                    margin: const EdgeInsets.symmetric(vertical: 4),
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    decoration: BoxDecoration(
                      color: const Color(0xFF2E3547),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: Text(
                      _messages[index],
                      style: const TextStyle(color: Colors.white, fontSize: 13),
                    ),
                  ),
                );
              },
            ),
          ),

          // Quick replies
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _quickActionChip("👍 Thumbs up", () => _sendReply("👍")),
                  _quickActionChip("On it! 🚀", () => _sendReply("On it! 🚀")),
                  _quickActionChip("Call you later 📞", () => _sendReply("Call you later 📞")),
                ],
              ),
            ),
          ),

          // Input field
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: const BoxDecoration(
              color: Color(0xFF262C3A),
              borderRadius: BorderRadius.vertical(bottom: Radius.circular(24)),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Focus(
                    onFocusChange: (hasFocus) async {
                      if (hasFocus) {
                        await FlutterOverlayWindow.updateFlag(OverlayFlag.focusPointer);
                      } else {
                        await FlutterOverlayWindow.updateFlag(OverlayFlag.defaultFlag);
                      }
                    },
                    child: TextField(
                      controller: _replyController,
                      style: const TextStyle(color: Colors.white, fontSize: 13),
                      decoration: const InputDecoration(
                        hintText: "Type reply...",
                        hintStyle: TextStyle(color: Colors.white38, fontSize: 13),
                        border: InputBorder.none,
                        isDense: true,
                      ),
                      onSubmitted: _sendReply,
                    ),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.send_rounded, color: Color(0xFF00C6FF), size: 20),
                  onPressed: () {
                    _sendReply(_replyController.text);
                    _replyController.clear();
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _quickActionChip(String label, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.only(right: 6),
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          color: const Color(0xFF333B4F),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Text(
          label,
          style: const TextStyle(color: Colors.white70, fontSize: 11),
        ),
      ),
    );
  }

  void _sendReply(String text) {
    if (text.trim().isEmpty) return;
    FlutterOverlayWindow.shareData(text);
    setState(() {
      _messages.add("You: $text");
    });
  }
}
