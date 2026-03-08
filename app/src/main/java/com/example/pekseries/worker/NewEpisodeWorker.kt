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
        Log.d("PekWorker", "Фоновая проверка новых серий запущена!")

        try {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid

            if (userId == null) return Result.success()

            val db = FirebaseFirestore.getInstance()
            val repository = SeriesRepository()

            val subsSnapshot = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .get()
                .await()

            val subscribedIds = subsSnapshot.documents.map { it.id }
            if (subscribedIds.isEmpty()) return Result.success()

            val todayShows = repository.getTodayEpisodes()

            val matches = todayShows.filter { show ->
                subscribedIds.contains(show.id)
            }

            matches.forEach { show ->
                sendNotification(
                    title = "New Episode Today!",
                    message = "${show.title} - ${show.episode} is out!"
                )
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("PekWorker", "Ошибка при проверке серий", e)
            return Result.retry() // Попробовать позже, если пропал интернет
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