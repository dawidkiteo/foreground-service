package com.snowcookiesports.foreground_service

import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


const val mainMethodChannelName = "com.snowcookiesports.foreground_service"

class ForegroundServicePlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, mainMethodChannelName)
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "startForegroundService" -> startForegroundService(call, result)
            "stopForegroundService" -> stopForegroundService(result)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun startForegroundService(call: MethodCall, result: Result) {
        val icon = call.argument<String>("icon")
        val title = call.argument<String>("title")
        val content = call.argument<String>("content")
        val stopIcon = call.argument<String>("stopIcon")
        val stopText = call.argument<String>("stopText")
        val callbackId = call.argument<Long>("callbackId")
        val stopCallbackId = call.argument<Long>("stopCallbackId");

        val intent = Intent(context, FlutterForegroundService::class.java)
        intent.action = "start"
        intent.putExtra("icon", icon)
        intent.putExtra("title", title)
        intent.putExtra("content", content)
        intent.putExtra("stopIcon", stopIcon)
        intent.putExtra("stopText", stopText)
        intent.putExtra("callbackId", callbackId)
        intent.putExtra("stopCallbackId", stopCallbackId)

        ContextCompat.startForegroundService(context, intent)

        result.success(true)
    }

    private fun stopForegroundService(result: Result) {
        val intent = Intent(context, FlutterForegroundService::class.java)
        intent.action = "stop"
        context.stopService(intent)

        result.success(true)
    }
}
