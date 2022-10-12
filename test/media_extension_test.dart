import 'package:flutter_test/flutter_test.dart';
import 'package:media_extension/media_extension.dart';
import 'package:media_extension/media_extension_method_channel.dart';
import 'package:media_extension/media_extension_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockMediaExtensionPlatform
    with MockPlatformInterfaceMixin
    implements MediaExtensionPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<bool?> setAs(String uri, String mimeType) {
    // TODO: implement setAs
    throw UnimplementedError();
  }
}

void main() {
  final MediaExtensionPlatform initialPlatform =
      MediaExtensionPlatform.instance;

  test('$MethodChannelMediaExtension is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelMediaExtension>());
  });

  test('getPlatformVersion', () async {
    MediaExtension mediaExtensionPlugin = MediaExtension();
    MockMediaExtensionPlatform fakePlatform = MockMediaExtensionPlatform();
    MediaExtensionPlatform.instance = fakePlatform;

    expect(await mediaExtensionPlugin.getPlatformVersion(), '42');
  });
}
