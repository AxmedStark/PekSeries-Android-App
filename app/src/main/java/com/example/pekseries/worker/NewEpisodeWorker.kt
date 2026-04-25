package com.example.pekseries.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pekseries.MainActivity
import com.example.pekseries.data.repository.SeriesRepository

class NewEpisodeWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = SeriesRepository()
        val prefs = appContext.getSharedPreferences("pek_notifications", Context.MODE_PRIVATE)
        val notifiedIds = prefs.getStringSet("notified_episodes", mutableSetOf()) ?: mutableSetOf()

        return try {
            val newlyAired = repository.getNewlyAiredEpisodesToNotify(notifiedIds)

            if (newlyAired.isNotEmpty()) {
                val newNotifiedIds = notifiedIds.toMutableSet()

                for (item in newlyAired) {
                    val title = "New episode: ${item.showTitle}"
                    val message = item.episodeString

                    repository.saveNotification(title, message)
                    sendNotification(title, message, item.episodeId.hashCode())

                    newNotifiedIds.add(item.episodeId)
                }

                prefs.edit().putStringSet("notified_episodes", newNotifiedIds).apply()
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String, notificationId: Int) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pek_series_new_episodes"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New Episodes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}