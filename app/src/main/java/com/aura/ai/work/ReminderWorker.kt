package com.aura.ai.work

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aura.ai.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Posts a daily "come back to Aura" reminder notification. */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hasPermission = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val notification = NotificationCompat.Builder(applicationContext, NotificationScheduler.CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Aura is ready")
                .setContentText("Got something on your mind? Start a new conversation.")
                .setAutoCancel(true)
                .build()
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(1001, notification)
        }
        return Result.success()
    }
}
