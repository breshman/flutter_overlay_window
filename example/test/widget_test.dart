import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_overlay_window_example/main.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const MethodChannel channel = MethodChannel('x-slayer/overlay_channel');
  const MethodChannel overlayChannel = MethodChannel('x-slayer/overlay');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        switch (methodCall.method) {
          case 'checkPermission':
            return true;
          case 'isOverlayActive':
            return false;
          default:
            return true;
        }
      },
    );

    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      overlayChannel,
      (MethodCall methodCall) async {
        return true;
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(overlayChannel, null);
  });

  testWidgets('Verify Overlay Showcase boots and renders controls', (WidgetTester tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(() {
      tester.view.resetPhysicalSize();
      tester.view.resetDevicePixelRatio();
    });

    await tester.pumpWidget(const MyApp());
    await tester.pumpAndSettle();

    expect(find.text('Overlay Showcase'), findsOneWidget);
    expect(find.text('No Active Overlay'), findsOneWidget);
    expect(find.text('Overlay Designs'), findsOneWidget);
    expect(find.text('Messenger Chat Head'), findsOneWidget);
    expect(find.text('Dynamic Island Capsule'), findsOneWidget);
    expect(find.text('Quick Note Pad'), findsOneWidget);
  });
}
