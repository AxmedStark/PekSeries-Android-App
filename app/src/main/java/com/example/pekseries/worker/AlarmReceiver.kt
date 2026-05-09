package com.example.pekseries.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pekseries.MainActivity
import com.example.pekseries.data.repository.SeriesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: SeriesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val profilePrefs = context.getSharedPreferences("pek_prefs", Context.MODE_PRIVATE)
        val pushEnabled = profilePrefs.getBoolean("push_enabled", true)

        if (!pushEnabled) return

        val pendingResult = goAsync()

        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val prefs = context.getSharedPreferences("pek_notifications", Context.MODE_PRIVATE)
                val notifiedIds = prefs.getStringSet("notified_episodes", mutableSetOf()) ?: mutableSetOf()
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

                val newlyAired = repository.getNewlyAiredEpisodesToNotify(notifiedIds)

                if (newlyAired.isNotEmpty()) {
                    val newNotifiedIds = notifiedIds.toMutableSet()

                    for (item in newlyAired) {
                        val localTime = timeFormatter.format(item.airTimeInstant)
                        val title = "New episode: ${item.showTitle}"
                        val message = "${item.episodeString} at $localTime"

                        repository.saveNotification(title, message)
                        sendNotification(context, title, message, item.episodeId.hashCode())

                        newNotifiedIds.add(item.episodeId)
                    }

                    prefs.edit().putStringSet("notified_episodes", newNotifiedIds).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                PekAlarmManager.scheduleNextAlarm(context)
                pendingResult.finish()
            }
        }
    }

    private fun sendNotification(context: Context, title: String, message: String, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pek_series_new_episodes"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New Episodes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about new episodes of subscribed series"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}