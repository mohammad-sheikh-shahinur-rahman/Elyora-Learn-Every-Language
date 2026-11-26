package com.itamadersomajinc.elyora

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

enum class PlayUpdateStatus {
    CHECKING,
    UPDATE_PROMPTED,
    DOWNLOADED_READY,
    UP_TO_DATE,
    PLAY_FALLBACK_AVAILABLE
}

/**
 * Google Play compliant In-App Update helper using Play Core AppUpdateManager.
 * No third-party APK downloads or unknown-source sideloading.
 */
object Updater {

    fun currentVersion(ctx: Context): String = try {
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "2.0.0"
    } catch (_: Exception) {
        "2.0.0"
    }

    fun manager(ctx: Context): AppUpdateManager = AppUpdateManagerFactory.create(ctx.applicationContext)

    fun checkPlayUpdate(
        ctx: Context,
        launcher: ActivityResultLauncher<IntentSenderRequest>?,
        onStatus: (PlayUpdateStatus) -> Unit
    ) {
        onStatus(PlayUpdateStatus.CHECKING)
        try {
            val mgr = manager(ctx)
            mgr.appUpdateInfo
                .addOnSuccessListener { info ->
                    when {
                        info.installStatus() == InstallStatus.DOWNLOADED -> {
                            onStatus(PlayUpdateStatus.DOWNLOADED_READY)
                        }
                        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) &&
                            launcher != null -> {
                            val started = runCatching {
                                mgr.startUpdateFlowForResult(
                                    info,
                                    launcher,
                                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                                )
                            }.getOrDefault(false)
                            if (started) {
                                onStatus(PlayUpdateStatus.UPDATE_PROMPTED)
                            } else {
                                onStatus(PlayUpdateStatus.PLAY_FALLBACK_AVAILABLE)
                            }
                        }
                        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                            onStatus(PlayUpdateStatus.PLAY_FALLBACK_AVAILABLE)
                        }
                        else -> {
                            onStatus(PlayUpdateStatus.UP_TO_DATE)
                        }
                    }
                }
                .addOnFailureListener {
                    // Expected on sideloaded debug builds or devices without Play Store context
                    onStatus(PlayUpdateStatus.PLAY_FALLBACK_AVAILABLE)
                }
        } catch (_: Exception) {
            onStatus(PlayUpdateStatus.PLAY_FALLBACK_AVAILABLE)
        }
    }

    fun checkDownloadedOnResume(ctx: Context, onDownloaded: () -> Unit) {
        runCatching {
            manager(ctx).appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    onDownloaded()
                }
            }
        }
    }

    fun registerFlexibleListener(
        mgr: AppUpdateManager,
        onDownloaded: () -> Unit
    ): InstallStateUpdatedListener {
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                onDownloaded()
            }
        }
        runCatching { mgr.registerListener(listener) }
        return listener
    }

    fun completePlayUpdate(ctx: Context) {
        runCatching { manager(ctx).completeUpdate() }
    }

    fun openPlayStore(ctx: Context) {
        val pkg = ctx.packageName
        try {
            ctx.startActivity(
                Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            runCatching {
                ctx.startActivity(
                    Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$pkg".toUri())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }
}
