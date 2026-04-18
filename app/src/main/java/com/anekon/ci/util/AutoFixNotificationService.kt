package com.anekon.ci.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.anekon.ci.R
import com.anekon.ci.ui.MainActivity

/**
 * Servicio de notificaciones para AutoFix
 */
class AutoFixNotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_ID_AUTO_FIX = "autofix_channel"
        const val CHANNEL_NAME_AUTO_FIX = "AutoFix Notifications"
        const val CHANNEL_DESC_AUTO_FIX = "Notificaciones de builds fallidos y fixes aplicados"

        const val NOTIFICATION_ID_BUILD_FAILED = 1001
        const val NOTIFICATION_ID_FIX_APPLIED = 1002
        const val NOTIFICATION_ID_ANALYSIS_COMPLETE = 1003
        const val NOTIFICATION_ID_FIX_FAILED = 1004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_AUTO_FIX,
                CHANNEL_NAME_AUTO_FIX,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_AUTO_FIX
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Notifica cuando un build falla
     */
    fun notifyBuildFailed(
        projectName: String,
        workflowName: String,
        branch: String,
        errorMessage: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "autofix")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_AUTO_FIX)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Build Fallido: $workflowName")
            .setContentText("$projectName • $branch")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(errorMessage ?: "El build falló en $projectName/$workflowName")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Analizar con IA",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BUILD_FAILED,
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }

    /**
     * Notifica cuando el análisis de IA está completo
     */
    fun notifyAnalysisComplete(
        projectName: String,
        workflowName: String,
        errorType: String?,
        confidence: Float
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "autofix")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val confidenceText = "${(confidence * 100).toInt()}% confianza"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_AUTO_FIX)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Análisis Listo: $errorType")
            .setContentText("$projectName • $confidenceText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("El análisis de IA para $workflowName está listo. Tipo de error: $errorType con $confidenceText de confianza.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Ver Análisis",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_ANALYSIS_COMPLETE,
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }

    /**
     * Notifica cuando se aplicó un fix exitosamente
     */
    fun notifyFixApplied(
        projectName: String,
        workflowName: String,
        filePath: String,
        commitSha: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "projects")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val commitInfo = commitSha?.let { "\nCommit: ${it.take(7)}" } ?: ""

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_AUTO_FIX)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Fix Aplicado: $workflowName")
            .setContentText("$projectName • $filePath")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Se aplicó un fix automático a $filePath en $projectName.$commitInfo\n\nEl fix fue aplicado exitosamente. Puedes verificar el cambio en GitHub.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_FIX_APPLIED,
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }

    /**
     * Notifica cuando el fix automático falló
     */
    fun notifyFixFailed(
        projectName: String,
        workflowName: String,
        reason: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "autofix")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_AUTO_FIX)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Fix Falló: $workflowName")
            .setContentText("$projectName • Se requiere intervención manual")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("El fix automático no pudo ser aplicado a $workflowName en $projectName.\n\nRazón: ${reason ?: "Error desconocido"}\n\nSe necesita revisión manual del error.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Revisar Manualmente",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_FIX_FAILED,
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }

    /**
     * Cancela todas las notificaciones de AutoFix
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_BUILD_FAILED)
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_ANALYSIS_COMPLETE)
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_FIX_APPLIED)
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_FIX_FAILED)
    }
}
