import 'package:flutter/material.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

class DynamicIslandOverlay extends StatefulWidget {
  const DynamicIslandOverlay({super.key});

  @override
  State<DynamicIslandOverlay> createState() => _DynamicIslandOverlayState();
}

class _DynamicIslandOverlayState extends State<DynamicIslandOverlay> {
  bool _isExpanded = false;
  bool _isPlaying = true;
  double _progress = 0.42;

  Future<void> _toggleExpand() async {
    if (_isExpanded) {
      await FlutterOverlayWindow.resizeOverlay(200, 56, true);
    } else {
      await FlutterOverlayWindow.resizeOverlay(340, 240, false);
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
          duration: const Duration(milliseconds: 250),
          curve: Curves.easeInOut,
          width: _isExpanded ? 340 : 200,
          height: _isExpanded ? 240 : 56,
          padding: EdgeInsets.symmetric(
            horizontal: _isExpanded ? 16 : 12,
            vertical: _isExpanded ? 14 : 8,
          ),
          decoration: BoxDecoration(
            color: const Color(0xFF121316).withValues(alpha: 0.95),
            borderRadius: BorderRadius.circular(_isExpanded ? 28 : 28),
            border: Border.all(color: Colors.white.withValues(alpha: 0.12), width: 1),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.4),
                blurRadius: 18,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          child: _isExpanded ? _buildExpandedView() : _buildCollapsedPill(),
        ),
      ),
    );
  }

  Widget _buildCollapsedPill() {
    return GestureDetector(
      onTap: _toggleExpand,
      behavior: HitTestBehavior.opaque,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)],
              ),
              borderRadius: BorderRadius.circular(18),
            ),
            child: const Icon(Icons.music_note, color: Colors.white, size: 20),
          ),
          const SizedBox(width: 10),
          const Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "Starboy",
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 12,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                Text(
                  "The Weeknd",
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 10,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          IconButton(
            padding: EdgeInsets.zero,
            constraints: const BoxConstraints(),
            icon: Icon(
              _isPlaying ? Icons.pause_circle_filled : Icons.play_circle_fill,
              color: Colors.white,
              size: 26,
            ),
            onPressed: () {
              setState(() => _isPlaying = !_isPlaying);
              FlutterOverlayWindow.shareData("music_toggle");
            },
          ),
        ],
      ),
    );
  }

  Widget _buildExpandedView() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)],
                    ),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: const Icon(Icons.album, color: Colors.white, size: 28),
                ),
                const SizedBox(width: 12),
                const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "Starboy",
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                    Text(
                      "The Weeknd • Daft Punk",
                      style: TextStyle(
                        color: Colors.white60,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ],
            ),
            IconButton(
              icon: const Icon(Icons.close_fullscreen, color: Colors.white60, size: 20),
              onPressed: _toggleExpand,
            ),
          ],
        ),
        const Spacer(),
        SliderTheme(
          data: SliderThemeData(
            trackHeight: 3,
            thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 5),
            overlayShape: const RoundSliderOverlayShape(overlayRadius: 10),
            activeTrackColor: Colors.deepPurpleAccent,
            inactiveTrackColor: Colors.white24,
            thumbColor: Colors.white,
          ),
          child: Slider(
            value: _progress,
            onChanged: (val) => setState(() => _progress = val),
          ),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 6),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                "01:32",
                style: TextStyle(color: Colors.white.withValues(alpha: 0.5), fontSize: 10),
              ),
              Text(
                "03:50",
                style: TextStyle(color: Colors.white.withValues(alpha: 0.5), fontSize: 10),
              ),
            ],
          ),
        ),
        const Spacer(),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            IconButton(
              icon: const Icon(Icons.shuffle, color: Colors.white60, size: 20),
              onPressed: () {},
            ),
            IconButton(
              icon: const Icon(Icons.skip_previous, color: Colors.white, size: 28),
              onPressed: () {},
            ),
            GestureDetector(
              onTap: () => setState(() => _isPlaying = !_isPlaying),
              child: Container(
                width: 44,
                height: 44,
                decoration: const BoxDecoration(
                  color: Colors.white,
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  _isPlaying ? Icons.pause : Icons.play_arrow,
                  color: Colors.black,
                  size: 26,
                ),
              ),
            ),
            IconButton(
              icon: const Icon(Icons.skip_next, color: Colors.white, size: 28),
              onPressed: () {},
            ),
            IconButton(
              icon: const Icon(Icons.close, color: Colors.redAccent, size: 20),
              onPressed: () => FlutterOverlayWindow.closeOverlay(),
            ),
          ],
        ),
      ],
    );
  }
}
