import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:media_extension/media_extension_method_channel.dart';

void main() {
  MethodChannelMediaExtension platform = MethodChannelMediaExtension();
  const MethodChannel channel = MethodChannel('media_extension');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
