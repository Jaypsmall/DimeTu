package com.jaylizapp.dimetu

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class TrackerService : Service() {

    private val handler = Handler(Looper.getMainLooper())

    private var lastMsgId: Long? = null
    private var lastStatus: Int? = null
    private var watchStartTime: Long = 0L

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkWhatsAppStatus()
            handler.postDelayed(this, 5000)
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        NotificationHelper.createNotificationChannel(this)

        // Inicia el servicio en primer plano
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

        // Inicia el demonio root
        RootReader.startRootDaemon(this)

        TrackerRepository.setServiceRunning(true)
        TrackerRepository.addLog("🚀 Rastreador iniciado")

        handler.post(checkRunnable)

        return START_STICKY
    }

    private fun checkWhatsAppStatus() {

        val selectedJid = TrackerRepository.selectedChatJid.value

        Thread {

            val ready = RootReader.isDbReady(this)

            if (!ready) {

                TrackerRepository.addLog("⏳ Esperando copia de la BD...")

                return@Thread
            }

            val message =
                if (selectedJid != null) {

                    WhatsAppDatabase.getLatestSentMessageForChat(
                        this,
                        selectedJid
                    )

                } else {

                    WhatsAppDatabase.getLatestSentMessageGlobal(
                        this
                    )

                }

            if (message == null)
                return@Thread

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

                val statusText = getStatusLabel(currentStatus)
                
                TrackerRepository.addLog("""
                    📩 NUEVO MENSAJE
                    ID: $currentId
                    TEXTO: ${message.text ?: "(sin texto)"}
                    ESTADO: $statusText
                """.trimIndent())

                return@Thread
            }

            if (currentStatus == lastStatus)
                return@Thread

            val elapsed =
                (System.currentTimeMillis() - watchStartTime) / 1000

            val oldStatusText = getStatusLabel(lastStatus ?: -1)
            val newStatusText = getStatusLabel(currentStatus)

            TrackerRepository.addLog(
                "Cambio: $oldStatusText → $newStatusText (${elapsed}s)"
            )

            if ((lastStatus == 0 || lastStatus == 4)
                && currentStatus == 5
            ) {

                NotificationHelper.showDeliveredNotification(
                    this,
                    message.text
                )

                TrackerRepository.addLog(
                    "🔔 MENSAJE ENTREGADO ✓✓"
                )

                // Dejamos de vigilar ese mensaje
                lastStatus = currentStatus
                handler.removeCallbacks(checkRunnable)

                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()

                return@Thread
            }
            
            if (currentStatus == 13) {
                 TrackerRepository.addLog(
                    "👁️ MENSAJE LEÍDO"
                )
            }

            lastStatus = currentStatus

        }.start()
    }

    private fun getStatusLabel(status: Int): String {
        return when (status) {
            0 -> "⏳ Pendiente"
            4 -> "✓ Enviado"
            5 -> "✓✓ Entregado"
            13 -> "👁️ Leído"
            else -> "Estado $status"
        }
    }

    override fun onDestroy() {

        handler.removeCallbacks(checkRunnable)

        RootReader.stopRootDaemon()

        TrackerRepository.setServiceRunning(false)

        TrackerRepository.addLog("🛑 Rastreador detenido")

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}