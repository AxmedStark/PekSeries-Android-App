package com.example.pekseries.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pekseries.MainActivity
import com.example.pekseries.R
import com.example.pekseries.data.repository.SeriesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NewEpisodeWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = SeriesRepository()
        Log.d("PekWorker", "--- ЗАПУСК ПРОВЕРКИ РАСПИСАНИЯ ---")

        return try {
            val upcoming = repository.getUpcomingSubscribedEpisodes()
            // Получаем дату в формате YYYY-MM-DD
            val today = java.time.LocalDate.now().toString()

            val outToday = upcoming.filter { it.time == today }

            if (outToday.isNotEmpty()) {
                val names = outToday.joinToString(", ") { it.title }
                Log.d("PekWorker", "НАЙДЕНО: $names")

                repository.saveNotification("Новые серии сегодня!", names)
                sendNotification("Премьеры сегодня! 🍿", names)
            } else {
                Log.d("PekWorker", "НИЧЕГО НЕ НАЙДЕНО на дату: $today")
                // Для теста можно отправить пуш "Пусто", чтобы убедиться что воркер живой:
                 sendNotification("Проверка PekSeries", "Новых серий пока нет")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("PekWorker", "ОШИБКА ПРОВЕРКИ: ${e.message}")
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
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

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
}