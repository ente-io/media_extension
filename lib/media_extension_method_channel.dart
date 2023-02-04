import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:media_extension/media_extension_action_types.dart';

import 'media_extension_platform_interface.dart';

/// An implementation of [MediaExtensionPlatform] that uses method channels.
class MethodChannelMediaExtension extends MediaExtensionPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('media_extension');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> setAs(String uri, String mimeType,
      {String title = "Set as"}) async {
    try {
      final result =
          await methodChannel.invokeMethod('setAs', <String, dynamic>{
        'uri': uri,
        'mimeType': mimeType,
        'title': title,
      });
      if (result != null) return result as bool;
    } on PlatformException catch (e) {
      debugPrint(e.message);
    }
    return false;
  }

  @override
  Future<bool> edit(
    String uri,
    String mimeType, {
    String title = 'Edit',
  }) async {
    try {
      final result = await methodChannel.invokeMethod('edit', <String, dynamic>{
        'uri': uri,
        'mimeType': mimeType,
        'title': title,
      });
      if (result != null) return result as bool;
    } on PlatformException catch (e) {
      debugPrint(e.message);
    }
    return false;
  }

  @override
  Future<bool> openWith(
    String uri,
    String mimeType, {
    String title = 'Open With',
  }) async {
    try {
      final result =
          await methodChannel.invokeMethod('openWith', <String, dynamic>{
        'uri': uri,
        'mimeType': mimeType,
        'title': title,
      });
      if (result != null) return result as bool;
    } on PlatformException catch (e) {
      debugPrint(e.message);
    }
    return false;
  }

  @override
  Future<MediaExtentionAction> getIntentAction() async {
    final Completer<MediaExtentionAction> completer =
        Completer<MediaExtentionAction>();
    methodChannel.setMethodCallHandler((call) async {
      final List<String> args = call.arguments.toString().split("!");
      final String action = args[0];
      final String uri = args[1];
      MediaExtentionAction intentAction =
          MediaExtentionAction(action: actionStringify(action), uri: uri);
      completer.complete(intentAction);
    });
    return completer.future;
  }

  @override
  Future<void> setResult(String uri) async {
    try {
      await methodChannel.invokeMethod('setResult', {
        'uri': uri,
      });
    } on PlatformException catch (e) {
      debugPrint(e.message);
    }
  }
}
