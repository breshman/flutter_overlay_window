import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

void main() {
  group('OverlayPosition Model Tests', () {
    test('OverlayPosition toMap and fromMap conversion', () {
      const pos = OverlayPosition(45, 90);
      final map = pos.toMap();

      expect(map['x'], 45);
      expect(map['y'], 90);

      final fromMap = OverlayPosition.fromMap(const {'x': 45.0, 'y': 90.0});
      expect(fromMap.x, 45.0);
      expect(fromMap.y, 90.0);
    });

    test('OverlayPosition fromMap with null or empty returns default (0, 0)', () {
      final defaultPos = OverlayPosition.fromMap(null);
      expect(defaultPos.x, 0.0);
      expect(defaultPos.y, 0.0);

      final emptyPos = OverlayPosition.fromMap(const {});
      expect(emptyPos.x, 0.0);
      expect(emptyPos.y, 0.0);
    });

    test('OverlayPosition toString contains coordinates', () {
      const pos = OverlayPosition(120, -50);
      expect(pos.toString(), contains('OverlayPosition{x=120.0, y=-50.0}'));
    });
  });

  group('Overlay Configuration Constants', () {
    test('WindowSize constants are defined correctly', () {
      expect(WindowSize.fullCover, -1999);
      expect(WindowSize.matchParent, -1);
    });

    test('OverlayAlignment enum values', () {
      expect(OverlayAlignment.center.name, 'center');
      expect(OverlayAlignment.topLeft.name, 'topLeft');
      expect(OverlayAlignment.bottomRight.name, 'bottomRight');
    });

    test('PositionGravity enum values', () {
      expect(PositionGravity.none.name, 'none');
      expect(PositionGravity.auto.name, 'auto');
      expect(PositionGravity.left.name, 'left');
      expect(PositionGravity.right.name, 'right');
    });

    test('OverlayFlag enum values', () {
      expect(OverlayFlag.defaultFlag.name, 'defaultFlag');
      expect(OverlayFlag.clickThrough.name, 'clickThrough');
      expect(OverlayFlag.focusPointer.name, 'focusPointer');
    });
  });
}
