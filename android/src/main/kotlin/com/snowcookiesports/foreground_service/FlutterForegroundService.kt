package com.snowcookiesports.foreground_service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain

private const val NOTIFICATION_ID = 555
private const val CHANNEL_ID = "Run"
private const val STOP_SERVICE_ACTION = "stop_action"

class FlutterForegroundService : Service() {
    private var backgroundEngine: FlutterEngine? = null
    private var stopCallbackId: Long? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        backgroundEngine?.serviceControlSurface?.detachFromService()
        backgroundEngine?.destroy()
        backgroundEngine = null
        super.onDestroy()
    }

    override fun onCreate() {
        FlutterMain.startInitialization(applicationContext)
        Log.i("FlutterForegroundService", "Flutter service initialization started")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "start" -> {
                val pm = applicationContext.packageManager
                val notificationIntent = pm.getLaunchIntentForPackage(applicationContext.packageName)
                val pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        0
                )

                val bundle = intent.extras

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                            CHANNEL_ID,
                            "flutter_foreground_service_channel",
                            NotificationManager.IMPORTANCE_DEFAULT
                    )
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                            .createNotificationChannel(channel)
                }
                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(getNotificationIcon(bundle?.getString("icon")!!))
                        .setContentTitle(bundle.getString("title"))
                        .setContentText(bundle.getString("content"))
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)

                // postponed, as I can not find any easy solution for calling stop method
//                val stopSelf = Intent(this, FlutterForegroundService::class.java)
//                stopSelf.action = STOP_SERVICE_ACTION
//                val pStopSelf = PendingIntent.getService(
//                        this,
//                        0,
//                        stopSelf,
//                        PendingIntent.FLAG_CANCEL_CURRENT
//                )
//                builder.addAction(
//                        getNotificationIcon(bundle.getString("stopIcon")!!),
//                        bundle.getString("stopText"),
//                        pStopSelf
//                )

                stopCallbackId = bundle.getLong("stopCallbackId")
                startForeground(NOTIFICATION_ID, builder.build())
                startFlutterEngine(bundle.getLong("callbackId"))
            }
            "stop" -> {
                stopForeground(STOP_FOREGROUND_DETACH)
            }
            STOP_SERVICE_ACTION -> {
                stopCallbackId?.run {
                    val callback: FlutterCallbackInformation = FlutterCallbackInformation.lookupCallbackInformation(this)
                    val dartCallback = DartExecutor.DartCallback(assets, FlutterMain.findAppBundlePath(), callback)
                    backgroundEngine?.dartExecutor?.executeDartCallback(dartCallback)
                }
            }
            else -> {
                println("Unhandled action: " + intent?.action)
            }
        }

        return START_STICKY
    }

    private fun startFlutterEngine(callbackHandle: Long) {
        Log.i("FlutterForegroundService", "Creating flutter engine...")
        FlutterMain.ensureInitializationComplete(applicationContext, null)

        val callback: FlutterCallbackInformation = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

        backgroundEngine = FlutterEngine(this, null, false)
        backgroundEngine?.apply { ForegroundServicePluginRegistrant.register(this) }
        backgroundEngine?.serviceControlSurface?.attachToService(this, null, true)

        val dartCallback = DartExecutor.DartCallback(assets, FlutterMain.findAppBundlePath(), callback)
        backgroundEngine?.dartExecutor?.executeDartCallback(dartCallback)
    }

    private fun getNotificationIcon(iconName: String): Int {
        return applicationContext.resources.getIdentifier(
                iconName,
                "drawable",
                applicationContext.packageName
        )
    }
}