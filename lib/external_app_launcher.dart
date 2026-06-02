import 'package:flutter/services.dart';

class ExternalAppLauncher {
  static const MethodChannel _channel = MethodChannel('external_app_launcher');

  static Future<bool> canOpen(String url) async {
    final result = await _channel.invokeMethod<bool>(
      'canOpen',
      <String, Object?>{'url': url},
    );
    return result ?? false;
  }

  static Future<void> open(
    String url, {
    bool useChooser = false,
    String chooserTitle = '选择应用',
  }) async {
    await _channel.invokeMethod<void>('open', <String, Object?>{
      'url': url,
      'useChooser': useChooser,
      'chooserTitle': chooserTitle,
    });
  }
}
