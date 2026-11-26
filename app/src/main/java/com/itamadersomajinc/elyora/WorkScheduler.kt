package com.itamadersomajinc.elyora

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class DailyStudyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val channelId = "elyora_daily_reminder"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return Result.success()

        val ch = NotificationChannel(
            channelId,
            "Daily Study Reminder · দৈনিক অনুশীলন",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(ch)

        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            applicationContext,
            101,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Elyora Daily Practice · আজকের ৫ মিনিটের অনুশীলন")
            .setContentText("Keep your streak alive! Test 12 tenses, vocabulary & offline AI coach.")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runCatching { nm.notify(1001, notif) }
        }
        return Result.success()
    }
}

class QwenModelDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val modelId = inputData.getString(KEY_MODEL_ID) ?: return Result.failure()
        val spec = QwenModelCatalog.byId(modelId)
        val notifId = 2000 + (spec.id.hashCode() and 0x0FFF)

        ensureChannel()
        runCatching { setForeground(createForegroundInfo(notifId, spec, 0, "Connecting…")) }

        var lastNotifUpdateMs = 0L
        val bridge = ElyoraAppProvider.get(applicationContext).llamaBridge
        val ok = bridge.runDownloadSuspend(spec) { pct, statusText ->
            setProgressAsync(workDataOf(KEY_PROGRESS to pct, KEY_MODEL_ID to modelId))
            val now = System.currentTimeMillis()
            if (now - lastNotifUpdateMs >= 1000L) {
                lastNotifUpdateMs = now
                runCatching { setForeground(createForegroundInfo(notifId, spec, pct, statusText)) }
            }
        }
        return if (ok) {
            postCompletionNotification(notifId + 1, spec)
            Result.success(workDataOf(KEY_MODEL_ID to modelId, KEY_PROGRESS to 100))
        } else {
            Result.failure(workDataOf(KEY_MODEL_ID to modelId))
        }
    }

    private fun ensureChannel() {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        val ch = NotificationChannel(
            CHANNEL_ID,
            "AI Model Downloads · মডেল ডাউনলোড",
            NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannel(ch)
    }

    private fun createForegroundInfo(
        notifId: Int,
        spec: QwenModelSpec,
        pct: Int,
        statusText: String
    ): ForegroundInfo {
        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            applicationContext,
            notifId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Downloading ${spec.displayName} (${spec.quant})")
            .setContentText(statusText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, pct.coerceIn(0, 100), pct == 0)
            .setContentIntent(pi)
            .addAction(android.R.drawable.ic_media_pause, "Pause", cancelIntent)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notifId, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notifId, notif)
        }
    }

    private fun postCompletionNotification(notifId: Int, spec: QwenModelSpec) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${spec.displayName} Ready · ডাউনলোড সম্পন্ন")
            .setContentText("Offline AI Tutor is ready to use without internet.")
            .setAutoCancel(true)
            .build()
        runCatching { nm.notify(notifId, notif) }
    }

    companion object {
        const val CHANNEL_ID = "elyora_model_downloads"
        const val KEY_URL = "url"
        const val KEY_FILENAME = "filename"
        const val KEY_MODEL_ID = "model_id"
        const val KEY_PROGRESS = "progress"
        const val KEY_ERROR = "error"
    }
}

object WorkScheduler {
    private const val DAILY_WORK_NAME = "elyora_daily_reminder_work"

    fun setDailyReminder(context: Context, enabled: Boolean) {
        val wm = WorkManager.getInstance(context.applicationContext)
        if (!enabled) {
            wm.cancelUniqueWork(DAILY_WORK_NAME)
            return
        }
        val req = PeriodicWorkRequestBuilder<DailyStudyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(12, TimeUnit.HOURS)
            .build()
        wm.enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }

    fun enqueueQwenDownload(context: Context, spec: QwenModelSpec): java.util.UUID {
        val wm = WorkManager.getInstance(context.applicationContext)
        val input: Data = workDataOf(
            QwenModelDownloadWorker.KEY_URL to spec.huggingFaceUrl,
            QwenModelDownloadWorker.KEY_FILENAME to spec.ggufFilename,
            QwenModelDownloadWorker.KEY_MODEL_ID to spec.id
        )
        val req = OneTimeWorkRequestBuilder<QwenModelDownloadWorker>()
            .setInputData(input)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .build()
        wm.enqueueUniqueWork(
            "qwen_dl_${spec.id}",
            ExistingWorkPolicy.REPLACE,
            req
        )
        return req.id
    }

    fun cancelQwenDownload(context: Context, modelId: String) {
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork("qwen_dl_$modelId")
    }
}
