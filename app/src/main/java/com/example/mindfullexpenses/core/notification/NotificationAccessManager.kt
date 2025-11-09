package com.example.mindfullexpenses.core.notification

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.example.mindfullexpenses.core.service.ExpenseTrackerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAccessManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val serviceComponent = ComponentName(context, ExpenseTrackerService::class.java)

    fun hasNotificationAccess(): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        if (enabledListeners.contains(context.packageName)) {
            // double check specific component
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return flat.split(":").any { it.equals(serviceComponent.flattenToString(), ignoreCase = true) }
        }
        return false
    }
}
