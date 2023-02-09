import 'package:media_extension/media_extension_action_types.dart';

import 'package:media_extension/media_extension_platform_interface.dart';

class MediaExtension {
  Future<String?> getPlatformVersion() =>
      MediaExtensionPlatform.instance.getPlatformVersion();

  Future<bool> setAs(String uri, String mimeType) =>
      MediaExtensionPlatform.instance.setAs(uri, mimeType);

  Future<bool> edit(String uri, String mimeType) =>
      MediaExtensionPlatform.instance.edit(uri, mimeType);

  Future<bool> openWith(String uri, String mimeType) =>
      MediaExtensionPlatform.instance.edit(uri, mimeType);

  Future<MediaExtentionAction> getIntentAction() =>
      MediaExtensionPlatform.instance.getIntentAction();

  Future<void> setResult(String uri) =>
      MediaExtensionPlatform.instance.setResult(uri);

  Future<String> getResolvedContent(String uri) =>
      MediaExtensionPlatform.instance.getResolvedContent(uri);
}
