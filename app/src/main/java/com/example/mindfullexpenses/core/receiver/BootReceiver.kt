package com.example.mindfullexpenses.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import com.example.mindfullexpenses.core.service.ExpenseTrackerService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val componentName = context.packageManager.getComponentName(context, ExpenseTrackerService::class.java)
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val isEnabled = enabledListeners?.contains(componentName.flattenToString()) == true

        if (!isEnabled) {
            Log.d(TAG, "Notification listener not yet enabled")
            return
        }

        context.startService(Intent(context, ExpenseTrackerService::class.java))
        Log.d(TAG, "Expense tracker service requested after boot")
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}

private fun android.content.pm.PackageManager.getComponentName(
    context: Context,
    serviceClass: Class<out NotificationListenerService>
) = android.content.ComponentName(context, serviceClass)


