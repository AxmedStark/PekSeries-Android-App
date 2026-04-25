package com.example.pekseries.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pekseries.data.repository.SeriesRepository
import java.time.LocalDate

class EpisodeCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("PekWorker", "Фоновая проверка новых серий запущена!")
        val repository = SeriesRepository()

        try {
            // 1. Берем наши будущие серии из репозитория
            val upcoming = repository.getUpcomingSubscribedEpisodes()

            // 2. Смотрим, какая сегодня дата
            val today = LocalDate.now().toString()

            // 3. Ищем сериалы, которые выходят ИМЕННО СЕГОДНЯ
            val outToday = upcoming.filter { it.time == today }

            if (outToday.isNotEmpty()) {
                // Если есть серии, формируем текст и отправляем пуш
                val title = "Новые серии уже здесь! 🍿"
                val body = outToday.joinToString(", ") { it.title }
                repository.saveNotification(title, body)
                showLocalNotification(title, "Сегодня выходят: $body")
            } else {
                Log.d("PekWorker", "Сегодня новых серий нет.")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("PekWorker", "Ошибка при фоновой проверке", e)
            return Result.retry() // Попробовать позже, если нет интернета
        }
    }

    // Тот самый код показа уведомления, который мы писали раньше
    private fun showLocalNotification(title: String, body: String) {
        val channelId = "pek_series_local"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Ежедневные проверки", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Твоя новая иконка подтянется сюда
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}