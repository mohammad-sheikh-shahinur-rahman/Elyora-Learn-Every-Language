package com.itamadersomajinc.elyora

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
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
        val urlStr = inputData.getString(KEY_URL) ?: return Result.failure()
        val filename = inputData.getString(KEY_FILENAME) ?: return Result.failure()
        val modelId = inputData.getString(KEY_MODEL_ID) ?: ""

        val modelsDir = File(applicationContext.filesDir, "models").apply { mkdirs() }
        val tmpFile = File(modelsDir, "$filename.part")
        val targetFile = File(modelsDir, filename)

        return try {
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
            }
            val total = conn.contentLengthLong.coerceAtLeast(1L)
            if (conn.responseCode !in 200..299) {
                return Result.failure(workDataOf(KEY_ERROR to "HTTP ${conn.responseCode}"))
            }
            conn.inputStream.use { input ->
                tmpFile.outputStream().use { out ->
                    val buf = ByteArray(16 * 1024)
                    var read = 0L
                    var lastPct = -1
                    while (true) {
                        if (isStopped) {
                            tmpFile.delete()
                            return Result.failure()
                        }
                        val n = input.read(buf)
                        if (n <= 0) break
                        out.write(buf, 0, n)
                        read += n
                        val pct = ((read * 100L) / total).toInt().coerceIn(0, 100)
                        if (pct != lastPct) {
                            lastPct = pct
                            setProgress(workDataOf(KEY_PROGRESS to pct, KEY_MODEL_ID to modelId))
                        }
                    }
                }
            }
            if (tmpFile.length() > 1024L) {
                if (targetFile.exists()) targetFile.delete()
                tmpFile.renameTo(targetFile)
                Result.success(workDataOf(KEY_MODEL_ID to modelId, KEY_PROGRESS to 100))
            } else {
                tmpFile.delete()
                Result.failure(workDataOf(KEY_ERROR to "Downloaded file is incomplete"))
            }
        } catch (e: Exception) {
            tmpFile.delete()
            Result.failure(workDataOf(KEY_ERROR to (e.localizedMessage ?: "Download failed")))
        }
    }

    companion object {
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
}
