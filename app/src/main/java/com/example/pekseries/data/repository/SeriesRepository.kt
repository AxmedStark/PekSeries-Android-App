package com.example.pekseries.data.repository

import android.util.Log
import com.example.pekseries.data.NetworkClient
import com.example.pekseries.model.Show
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class SeriesRepository {
    private val api = NetworkClient.api
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 1. Получить список серий на сегодня + статус "Просмотрено"
    suspend fun getTodayEpisodes(): List<Show> {
        val today = LocalDate.now().toString() // "2024-06-28"

        try {
            // А. Грузим данные из TVMaze
            val apiEpisodes = api.getSchedule(date = today)

            // Б. Грузим список просмотренных ID из Firebase (если юзер залогинен)
            val watchedIds = getWatchedEpisodeIdsFromFirebase()

            // В. Превращаем страшные данные API в красивые карточки для UI
            return apiEpisodes.map { dto ->
                Show(
                    id = dto.id.toString(),
                    title = dto.show.name,
                    episode = "S${dto.season} • E${dto.number}",
                    time = dto.airtime,
                    imageUrl = dto.show.image?.medium ?: "", // Если картинки нет, будет пусто
                    isNew = true,
                    // Проверяем: если ID эпизода есть в нашем списке, значит watched = true
                    // (нам нужно добавить поле isWatched в твою модель Show, сделаем это ниже)
                    isWatched = watchedIds.contains(dto.id.toString())
                )
            }
        } catch (e: Exception) {
            Log.e("PekRepo", "Error fetching episodes", e)
            return emptyList() // Или вернуть ошибку, чтобы показать пользователю
        }
    }

    // Вспомогательная функция: достать список ID из Firestore
    private suspend fun getWatchedEpisodeIdsFromFirebase(): Set<String> {
        val userId = auth.currentUser?.uid ?: return emptySet()

        return try {
            // Путь: users -> {userId} -> watched_episodes -> {episodeId}
            val snapshot = db.collection("users")
                .document(userId)
                .collection("watched_episodes")
                .get()
                .await()

            // Превращаем документы в список ID
            snapshot.documents.map { it.id }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // 2. Отметить серию как просмотренную (сохраняем в Firebase)
    suspend fun markEpisodeAsWatched(episodeId: String) {
        val userId = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "episodeId" to episodeId
        )

        try {
            db.collection("users")
                .document(userId)
                .collection("watched_episodes")
                .document(episodeId)
                .set(data) // .set создает или перезаписывает
                .await()
        } catch (e: Exception) {
            Log.e("PekRepo", "Failed to save to Firebase", e)
        }
    }
}