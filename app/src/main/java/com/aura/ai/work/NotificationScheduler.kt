package com.aura.ai.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_REMINDERS = "aura_reminders"
        const val CHANNEL_UPDATES = "aura_updates"
        private const val DAILY_WORK = "aura_daily_reminder"
    }

    fun createChannels() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Daily check-ins and conversation reminders" }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_UPDATES, "Updates", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Model updates and completed research" }
        )
    }

    fun scheduleDailyReminder(enabled: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) { wm.cancelUniqueWork(DAILY_WORK); return }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(8, TimeUnit.HOURS)
            .build()
        wm.enqueueUniquePeriodicWork(DAILY_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}
