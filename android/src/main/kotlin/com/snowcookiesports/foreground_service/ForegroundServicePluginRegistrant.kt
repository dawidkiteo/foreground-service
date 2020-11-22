package com.snowcookiesports.foreground_service

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.plugins.FlutterPlugin

object ForegroundServicePluginRegistrant {
    private val plugins: MutableSet<FlutterPlugin> = mutableSetOf()

    fun addPlugin(plugin: FlutterPlugin) {
        plugins.add(plugin)
    }

    fun register(engine: FlutterEngine) {
        engine.plugins.add(plugins)
    }
}