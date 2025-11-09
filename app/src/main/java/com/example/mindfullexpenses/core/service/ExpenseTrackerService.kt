package com.example.mindfullexpenses.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mindfullexpenses.R
import com.example.mindfullexpenses.core.autotrack.IngestionResult
import com.example.mindfullexpenses.core.autotrack.NotificationIngestionManager
import com.example.mindfullexpenses.core.autotrack.NotificationSources
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExpenseTrackerService : NotificationListenerService() {

    @Inject
    lateinit var ingestionManager: NotificationIngestionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ensureForeground()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        if (!supportedPackages.contains(packageName)) return

        val extras = sbn.notification?.extras ?: return
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        val text = when {
            !bigText.isNullOrBlank() -> bigText
            !lines.isNullOrEmpty() -> lines.joinToString(separator = "\n") { it.toString() }
            else -> extras.getCharSequence(Notification.EXTRA_TEXT)
        } ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
        val notificationKey = sbn.key

        serviceScope.launch {
            when (val result = ingestionManager.ingest(
                packageName = packageName,
                title = title,
                text = text,
                timestamp = sbn.postTime,
                notificationId = notificationKey
            )) {
                is IngestionResult.Success -> {
                    Log.d(TAG, "Logged expense for ${result.expense.cleanMerchant}")
                    // TODO dispatch UI update / show confirmation
                }

                is IngestionResult.Duplicate -> {
                    Log.d(TAG, "Duplicate expense ignored (${result.existingId})")
                }

                is IngestionResult.Failure -> {
                    Log.e(TAG, "Failed to log expense", result.throwable)
                }

                IngestionResult.Unhandled -> {
                    Log.v(TAG, "Notification from $packageName unhandled: ${text.take(120)}")
                }
            }
        }
    }

    private fun ensureForeground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.auto_tracking_active))
            .setContentText(getString(R.string.expenses_tracked_today, 0))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "ExpenseTrackerService"
        private const val CHANNEL_ID = "mindfull_tracking"
        private const val NOTIFICATION_ID = 101

        val supportedPackages: Set<String> = NotificationSources.supportedPackages
    }
}


