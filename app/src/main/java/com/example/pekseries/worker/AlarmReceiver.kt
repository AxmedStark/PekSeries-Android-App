package com.example.pekseries.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pekseries.data.repository.SeriesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repository = SeriesRepository()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val prefs = context.getSharedPreferences("pek_notifications", Context.MODE_PRIVATE)
            val notifiedIds = prefs.getStringSet("notified_episodes", mutableSetOf()) ?: mutableSetOf()

            val newlyAired = repository.getNewlyAiredEpisodesToNotify(notifiedIds)

            if (newlyAired.isNotEmpty()) {
                val newNotifiedIds = notifiedIds.toMutableSet()
                newlyAired.forEach { item ->
                    newNotifiedIds.add(item.episodeId)
                }
                prefs.edit().putStringSet("notified_episodes", newNotifiedIds).apply()
            }

            PekAlarmManager.scheduleNextAlarm(context)
        }
    }
}