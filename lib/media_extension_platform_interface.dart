import 'package:media_extension/media_extension_action_types.dart';
import 'package:media_extension/media_extension_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

abstract class MediaExtensionPlatform extends PlatformInterface {
  /// Constructs a MediaExtensionPlatform.
  MediaExtensionPlatform() : super(token: _token);

  static final Object _token = Object();

  static MediaExtensionPlatform _instance = MethodChannelMediaExtension();

  /// The default instance of [MediaExtensionPlatform] to use.
  ///
  /// Defaults to [MethodChannelMediaExtension].
  static MediaExtensionPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [MediaExtensionPlatform] when
  /// they register themselves.
  static set instance(MediaExtensionPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// abstract method for `getPlatformVersion` method.
  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// abstract method for `setAs` method.
  Future<bool> setAs(
    String uri,
    String mimeType, {
    String title = 'Set as',
  }) async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// abstract method for `edit` method.
  Future<bool> edit(
    String uri,
    String mimeType, {
    String title = 'Edit',
  }) async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// abstract method `openWith` method.
  Future<bool> openWith(
    String uri,
    String mimeType, {
    String title = 'Open with',
  }) async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// abstract method `getIntentAction` method.
  Future<MediaExtentionAction> getIntentAction() async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// abstract method `setResult` method.
  Future<void> setResult(String uri) async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
