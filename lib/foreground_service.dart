import 'dart:async';
import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class ForegroundService {
  static const MethodChannel _mainChannel = const MethodChannel('com.snowcookiesports.foreground_service');

  static Future<void> startForegroundService({
    @required String iconName,
    @required String title,
    @required Function entryMethod,
    @required Function stopCallback,
    String content = "",
    String stopIcon,
    String stopText,
  }) async {
    final entryMethodRawHandle = PluginUtilities.getCallbackHandle(entryMethod).toRawHandle();
    final stopCallbackRawHandle = PluginUtilities.getCallbackHandle(stopCallback).toRawHandle();
    await _mainChannel.invokeMethod("startForegroundService", <String, dynamic>{
      'icon': iconName,
      'title': title,
      'content': content,
      'stopIcon': stopIcon,
      'stopText': stopText,
      'callbackId': entryMethodRawHandle,
      'stopCallbackId': stopCallbackRawHandle,
    });
  }

  static Future<void> stopForegroundService() async {
    await _mainChannel.invokeMethod("stopForegroundService");
  }
}

enum EngineEvent { stopRequest }
