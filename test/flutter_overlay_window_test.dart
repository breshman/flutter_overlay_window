import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const MethodChannel channel = MethodChannel('x-slayer/overlay_channel');
  const MethodChannel overlayChannel = MethodChannel('x-slayer/overlay');
  const String messengerChannel = 'x-slayer/overlay_messenger';

  final List<MethodCall> channelCalls = [];
  final List<MethodCall> overlayChannelCalls = [];
  ByteData? lastSentMessage;

  setUp(() {
    channelCalls.clear();
    overlayChannelCalls.clear();
    lastSentMessage = null;

    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        channelCalls.add(methodCall);
        switch (methodCall.method) {
          case 'closeOverlay':
            return true;
          case 'isOverlayActive':
            return true;
          case 'checkPermission':
            return true;
          case 'requestPermission':
            return true;
          case 'showOverlay':
            return null;
          case 'updateNotification':
            return true;
          case 'moveOverlay':
            return true;
          case 'getOverlayPosition':
            return {'x': 120.0, 'y': 240.0};
          default:
            return null;
        }
      },
    );

    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      overlayChannel,
      (MethodCall methodCall) async {
        overlayChannelCalls.add(methodCall);
        switch (methodCall.method) {
          case 'updateFlag':
            return true;
          case 'resizeOverlay':
            return true;
          default:
            return null;
        }
      },
    );

    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMessageHandler(
      messengerChannel,
      (ByteData? message) async {
        lastSentMessage = message;
        return const StandardMessageCodec().encodeMessage('ack');
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(overlayChannel, null);
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMessageHandler(messengerChannel, null);
    FlutterOverlayWindow.disposeOverlayListener();
  });

  group('FlutterOverlayWindow Core Methods', () {
    test('checkPermission returns true when granted', () async {
      final result = await FlutterOverlayWindow.isPermissionGranted();
      expect(result, isTrue);
      expect(channelCalls.last.method, 'checkPermission');
    });

    test('requestPermission returns true when accepted', () async {
      final result = await FlutterOverlayWindow.requestPermission();
      expect(result, isTrue);
      expect(channelCalls.last.method, 'requestPermission');
    });

    test('showOverlay sends correct arguments map to channel', () async {
      await FlutterOverlayWindow.showOverlay(
        width: 300,
        height: 400,
        alignment: OverlayAlignment.center,
        flag: OverlayFlag.focusPointer,
        overlayTitle: "Test Title",
        overlayContent: "Test Content",
        enableDrag: true,
        enableDismiss: true,
        dismissIcon: "ic_trash",
        dismissIconColor: const Color(0xFFFFFFFF),
        dismissBackgroundColor: const Color(0xCCE53935),
        positionGravity: PositionGravity.auto,
        startPosition: const OverlayPosition(10, 20),
        notificationIcon: "ic_notification",
      );

      expect(channelCalls.last.method, 'showOverlay');
      final args = channelCalls.last.arguments as Map;
      expect(args['width'], 300);
      expect(args['height'], 400);
      expect(args['alignment'], 'center');
      expect(args['flag'], 'focusPointer');
      expect(args['overlayTitle'], 'Test Title');
      expect(args['overlayContent'], 'Test Content');
      expect(args['enableDrag'], isTrue);
      expect(args['enableDismiss'], isTrue);
      expect(args['dismissIcon'], 'ic_trash');
      expect(args['dismissIconColor'], 0xFFFFFFFF);
      expect(args['dismissBackgroundColor'], 0xCCE53935);
      expect(args['positionGravity'], 'auto');
      expect(args['startPosition'], {'x': 10, 'y': 20});
      expect(args['notificationIcon'], 'ic_notification');
    });

    test('showOverlay handles int colors for dismiss zone', () async {
      await FlutterOverlayWindow.showOverlay(
        enableDrag: true,
        enableDismiss: true,
        dismissIconColor: 0xFF123456,
        dismissBackgroundColor: 0x88ABCDEF,
      );

      final args = channelCalls.last.arguments as Map;
      expect(args['enableDismiss'], isTrue);
      expect(args['dismissIconColor'], 0xFF123456);
      expect(args['dismissBackgroundColor'], 0x88ABCDEF);
    });

    test('closeOverlay and isActive status checks', () async {
      final closed = await FlutterOverlayWindow.closeOverlay();
      expect(closed, isTrue);
      expect(channelCalls.last.method, 'closeOverlay');

      final active = await FlutterOverlayWindow.isActive();
      expect(active, isTrue);
      expect(channelCalls.last.method, 'isOverlayActive');
    });

    test('updateNotification sends title, content, icon', () async {
      final res = await FlutterOverlayWindow.updateNotification(
        title: "Updated",
        content: "Running",
        icon: "custom_icon",
      );
      expect(res, isTrue);
      expect(channelCalls.last.method, 'updateNotification');
      final args = channelCalls.last.arguments as Map;
      expect(args['title'], 'Updated');
      expect(args['content'], 'Running');
      expect(args['icon'], 'custom_icon');
    });

    test('updateFlag sends flag name to overlayChannel', () async {
      final res = await FlutterOverlayWindow.updateFlag(OverlayFlag.clickThrough);
      expect(res, isTrue);
      expect(overlayChannelCalls.last.method, 'updateFlag');
      expect(overlayChannelCalls.last.arguments, {'flag': 'clickThrough'});
    });

    test('resizeOverlay sends dimensions to overlayChannel', () async {
      final res = await FlutterOverlayWindow.resizeOverlay(250, 350, true);
      expect(res, isTrue);
      expect(overlayChannelCalls.last.method, 'resizeOverlay');
      final args = overlayChannelCalls.last.arguments as Map;
      expect(args['width'], 250);
      expect(args['height'], 350);
      expect(args['enableDrag'], isTrue);
    });

    test('moveOverlay and getOverlayPosition', () async {
      final moved = await FlutterOverlayWindow.moveOverlay(
        const OverlayPosition(50, 100),
      );
      expect(moved, isTrue);
      expect(channelCalls.last.method, 'moveOverlay');
      expect(channelCalls.last.arguments, {'x': 50, 'y': 100});

      final pos = await FlutterOverlayWindow.getOverlayPosition();
      expect(pos.x, 120.0);
      expect(pos.y, 240.0);
    });
  });

  group('Broadcast Stream & Message Sharing', () {
    test('overlayListener supports multiple listeners without Bad State', () async {
      final List<dynamic> events1 = [];
      final List<dynamic> events2 = [];

      final sub1 = FlutterOverlayWindow.overlayListener.listen(events1.add);
      final sub2 = FlutterOverlayWindow.overlayListener.listen(events2.add);

      // Simulate incoming binary message
      final encoded = const StandardMessageCodec().encodeMessage('hello_overlay');
      await TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .handlePlatformMessage(messengerChannel, encoded, (data) {});

      await pumpEventQueue();

      expect(events1, contains('hello_overlay'));
      expect(events2, contains('hello_overlay'));

      await sub1.cancel();
      await sub2.cancel();
    });

    test('disposeOverlayListener cleans stream and allows re-listening', () async {
      final sub = FlutterOverlayWindow.overlayListener.listen((_) {});
      await sub.cancel();

      // Dispose controller
      FlutterOverlayWindow.disposeOverlayListener();

      // Subscribing again must not throw
      final List<dynamic> newEvents = [];
      final newSub = FlutterOverlayWindow.overlayListener.listen(newEvents.add);

      final encoded = const StandardMessageCodec().encodeMessage('second_session');
      await TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .handlePlatformMessage(messengerChannel, encoded, (data) {});

      await pumpEventQueue();

      expect(newEvents, contains('second_session'));
      await newSub.cancel();
    });

    test('shareData sends message via StandardMessageCodec', () async {
      final response = await FlutterOverlayWindow.shareData({'key': 'val', 'num': 42});
      expect(response, 'ack');
      expect(lastSentMessage, isNotNull);
      final decoded = const StandardMessageCodec().decodeMessage(lastSentMessage);
      expect(decoded, {'key': 'val', 'num': 42});
    });
  });
}
