package com.anekon.ci.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.anekon.ci.ui.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio de notificaciones para Anekon
 * Maneja notificaciones push locales para builds, errores, y análisis IA
 */
@Singleton
class NotificationService @Inject constructor(
    private val context: Context
) {
    companion object {
        const val CHANNEL_BUILDS = "builds_channel"
        const val CHANNEL_AI = "ai_channel"
        const val CHANNEL_ERRORS = "errors_channel"

        const val NOTIFICATION_BUILD_SUCCESS = 1001
        const val NOTIFICATION_BUILD_FAILED = 1002
        const val NOTIFICATION_AI_ANALYSIS = 1003
        const val NOTIFICATION_AUTO_FIX = 1004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Channel for build notifications
            val buildsChannel = NotificationChannel(
                CHANNEL_BUILDS,
                "Build Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones sobre builds de GitHub Actions"
                enableVibration(true)
            }

            // Channel for AI notifications
            val aiChannel = NotificationChannel(
                CHANNEL_AI,
                "AI Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones de análisis de IA"
            }

            // Channel for errors
            val errorsChannel = NotificationChannel(
                CHANNEL_ERRORS,
                "Error Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de errores críticos"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(buildsChannel, aiChannel, errorsChannel)
            )
        }
    }

    /**
     * Notifica cuando un build es exitoso
     */
    fun notifyBuildSuccess(projectName: String, workflowName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "projects")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BUILDS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Build Exitoso")
            .setContentText("$projectName: $workflowName completado")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_BUILD_SUCCESS,
                notification
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Notifica cuando un build falla
     */
    fun notifyBuildFailed(projectName: String, workflowName: String, errorSummary: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "autofix")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ERRORS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Build Fallido")
            .setContentText("$projectName: $workflowName falló")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(errorSummary ?: "El build falló. Toca para ver el análisis de IA.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Ver Análisis IA",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_BUILD_FAILED,
                notification
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Notifica cuando el análisis de IA está listo
     */
    fun notifyAIAnalysisReady(projectName: String, summary: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_AI)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Análisis IA Completado")
            .setContentText("$projectName: $summary")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_AI_ANALYSIS,
                notification
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Notifica cuando un auto-fix fue aplicado
     */
    fun notifyAutoFixApplied(projectName: String, filePath: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_AI)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle("Auto-Fix Aplicado")
            .setContentText("$projectName: Fix aplicado a $filePath")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_AUTO_FIX,
                notification
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Cancela una notificación específica
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Cancela todas las notificaciones
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}