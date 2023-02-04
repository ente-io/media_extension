import 'package:media_extension/media_extension_action_types.dart';

import 'media_extension_platform_interface.dart';

class MediaExtension {
  Future<String?> getPlatformVersion() {
    return MediaExtensionPlatform.instance.getPlatformVersion();
  }

  Future<bool> setAs(String uri, String mimeType) {
    return MediaExtensionPlatform.instance.setAs(uri, mimeType);
  }

  Future<bool> edit(String uri, String mimeType) {
    return MediaExtensionPlatform.instance.edit(uri, mimeType);
  }

  Future<bool> openWith(String uri, String mimeType) {
    return MediaExtensionPlatform.instance.edit(uri, mimeType);
  }

  Future<MediaExtentionAction> getIntentAction() {
    return MediaExtensionPlatform.instance.getIntentAction();
  }

  Future<void> setResult(String uri) {
    return MediaExtensionPlatform.instance.setResult(uri);
  }
}
