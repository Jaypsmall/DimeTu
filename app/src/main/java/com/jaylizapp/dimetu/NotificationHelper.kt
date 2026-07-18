package com.jaylizapp.dimetu

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    private const val CHANNEL_ID = "tracker_channel"

    const val NOTIFICATION_ID = 1
    const val SERVICE_NOTIFICATION_ID = 2

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rastro WhatsApp",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Seguimiento del estado de mensajes de WhatsApp"
            enableLights(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 150, 250)
            setShowBadge(true)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showDeliveredNotification(
        context: Context,
        text: String?
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icono_dimetu)
            .setContentTitle("Mensaje entregado")
            .setContentText(
                if (text.isNullOrBlank()) {
                    "El mensaje ha llegado al destinatario"
                } else {
                    "Entregado: $text"
                }
            )
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun getServiceNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icono_dimetu)
            .setContentTitle("Rastro WA")
            .setContentText("Vigilando mensajes...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }

    fun updateServiceNotification(
        context: Context,
        status: Int,
        text: String?
    ) {
        val estado = when (status) {
            0 -> "Pendiente"
            4 -> "Enviado"
            5 -> "Entregado"
            13 -> "Leido"
            else -> "Estado $status"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icono_dimetu)
            .setContentTitle("Rastro WA")
            .setContentText("$estado - ${text.orEmpty()}")
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(SERVICE_NOTIFICATION_ID, notification)
    }
}
