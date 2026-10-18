import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

class QuickNoteOverlay extends StatefulWidget {
  const QuickNoteOverlay({super.key});

  @override
  State<QuickNoteOverlay> createState() => _QuickNoteOverlayState();
}

class _QuickNoteOverlayState extends State<QuickNoteOverlay> {
  final TextEditingController _controller = TextEditingController();
  Color _selectedColor = const Color(0xFFFEF3C7);
  final List<Color> _noteColors = const [
    Color(0xFFFEF3C7), // warm yellow
    Color(0xFFDCFCE7), // light green
    Color(0xFFE0E7FF), // soft indigo
    Color(0xFFFFE4E6), // rose
  ];

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Focus(
      onFocusChange: (hasFocus) async {
        if (hasFocus) {
          await FlutterOverlayWindow.updateFlag(OverlayFlag.focusPointer);
        } else {
          await FlutterOverlayWindow.updateFlag(OverlayFlag.defaultFlag);
        }
      },
      child: Material(
        color: Colors.transparent,
        child: Center(
          child: Container(
            width: 320,
            height: 290,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: _selectedColor,
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.25),
                  blurRadius: 16,
                  offset: const Offset(0, 6),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Row(
                      children: [
                        Icon(Icons.edit_note_rounded, color: Colors.black87, size: 22),
                        SizedBox(width: 6),
                        Text(
                          "Quick Note",
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 16,
                            color: Colors.black87,
                          ),
                        ),
                      ],
                    ),
                    Row(
                      children: [
                        for (final color in _noteColors)
                          GestureDetector(
                            onTap: () => setState(() => _selectedColor = color),
                            child: Container(
                              margin: const EdgeInsets.symmetric(horizontal: 3),
                              width: 16,
                              height: 16,
                              decoration: BoxDecoration(
                                color: color,
                                shape: BoxShape.circle,
                                border: Border.all(
                                  color: _selectedColor == color
                                      ? Colors.black87
                                      : Colors.black26,
                                  width: _selectedColor == color ? 2 : 1,
                                ),
                              ),
                            ),
                          ),
                        const SizedBox(width: 8),
                        GestureDetector(
                          onTap: () => FlutterOverlayWindow.closeOverlay(),
                          child: const Icon(Icons.close, size: 20, color: Colors.black54),
                        ),
                      ],
                    ),
                  ],
                ),
                const Divider(color: Colors.black12, height: 18),
                Expanded(
                  child: TextField(
                    controller: _controller,
                    maxLines: null,
                    expands: true,
                    style: const TextStyle(color: Colors.black87, fontSize: 14),
                    decoration: const InputDecoration(
                      hintText: "Type a quick reminder or note...",
                      hintStyle: TextStyle(color: Colors.black38, fontSize: 14),
                      border: InputBorder.none,
                    ),
                  ),
                ),
                const SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      "${_controller.text.length} chars",
                      style: const TextStyle(fontSize: 11, color: Colors.black45),
                    ),
                    ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.black87,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      icon: const Icon(Icons.check, size: 16),
                      label: const Text("Save & Send"),
                      onPressed: () {
                        final text = _controller.text.trim();
                        if (text.isNotEmpty) {
                          FlutterOverlayWindow.shareData({
                            "type": "note_saved",
                            "content": text,
                            "timestamp": DateTime.now().toIso8601String(),
                          });
                        }
                        FlutterOverlayWindow.closeOverlay();
                      },
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
