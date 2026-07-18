package com.jaylizapp.dimetu

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class TrackerService : Service() {

    private val handler = Handler(Looper.getMainLooper())

    private var lastChatId: Long? = null
    private var lastMsgId: Long? = null
    private var lastStatus: Int? = null
    private var watchStartTime: Long = 0L
    @Volatile private var checking = false

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkWhatsAppStatus()
            handler.postDelayed(this, 3000)
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.SERVICE_NOTIFICATION_ID,
                NotificationHelper.getServiceNotification(this),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(
                NotificationHelper.SERVICE_NOTIFICATION_ID,
                NotificationHelper.getServiceNotification(this)
            )
        }

        TrackerRepository.setServiceRunning(true)
        TrackerRepository.addLog("Rastreador iniciado")

        handler.removeCallbacks(checkRunnable)
        handler.post(checkRunnable)

        return START_STICKY
    }

    private fun checkWhatsAppStatus() {
        if (checking) return
        checking = true

        val selectedChatId = TrackerRepository.selectedChatId.value

        Thread {
            try {
                if (selectedChatId != lastChatId) {
                    lastChatId = selectedChatId
                    lastMsgId = null
                    lastStatus = null
                    watchStartTime = 0L
                }

                if (!RootSqlite.isAvailable()) {
                    TrackerRepository.addLog("Esperando root/sqlite3...")
                    return@Thread
                }

                val message =
                    if (selectedChatId != null) {
                        WhatsAppDatabase.getLatestSentMessageForChat(this, selectedChatId)
                    } else {
                        WhatsAppDatabase.getLatestSentMessageGlobal(this)
                    }

                if (message == null) {
                    TrackerRepository.addLog("No hay mensajes enviados para rastrear")
                    return@Thread
                }

                val currentId = message.id
                val currentStatus = message.status

                NotificationHelper.updateServiceNotification(
                    this,
                    currentStatus,
                    message.text
                )

                if (lastMsgId != currentId) {
                    lastMsgId = currentId
                    lastStatus = currentStatus
                    watchStartTime = System.currentTimeMillis()

                    TrackerRepository.addLog(
                        """
                        NUEVO MENSAJE
                        ID: $currentId
                        CHAT: ${message.chatId}
                        TEXTO: ${message.text ?: "(sin texto)"}
                        ESTADO: ${getStatusLabel(currentStatus)}
                        """.trimIndent()
                    )

                    return@Thread
                }

                if (currentStatus == lastStatus) return@Thread

                val elapsed = (System.currentTimeMillis() - watchStartTime) / 1000
                val oldStatusText = getStatusLabel(lastStatus ?: -1)
                val newStatusText = getStatusLabel(currentStatus)

                TrackerRepository.addLog(
                    "Cambio: $oldStatusText -> $newStatusText (${elapsed}s)"
                )

                val deliveredOrRead = currentStatus == 5 || currentStatus == 13
                val wasAlreadyDeliveredOrRead = lastStatus == 5 || lastStatus == 13

                if (deliveredOrRead && !wasAlreadyDeliveredOrRead) {
                    NotificationHelper.showDeliveredNotification(
                        this,
                        message.text
                    )

                    if (currentStatus == 13) {
                        TrackerRepository.addLog("MENSAJE LEIDO")
                    } else {
                        TrackerRepository.addLog("MENSAJE ENTREGADO")
                    }

                    lastStatus = currentStatus
                    handler.removeCallbacks(checkRunnable)

                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()

                    return@Thread
                }

                lastStatus = currentStatus
            } finally {
                checking = false
            }
        }.start()
    }

    private fun getStatusLabel(status: Int): String {
        return when (status) {
            0 -> "Pendiente (reloj)"
            4 -> "Enviado"
            5 -> "Entregado"
            13 -> "Leido"
            else -> "Estado $status"
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        TrackerRepository.setServiceRunning(false)
        TrackerRepository.addLog("Rastreador detenido")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
