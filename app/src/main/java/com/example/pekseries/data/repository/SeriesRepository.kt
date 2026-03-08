package com.example.pekseries.data.repository

import android.util.Log
import com.example.pekseries.data.NetworkClient
import com.example.pekseries.model.Episode
import com.example.pekseries.model.SearchResponseItem
import com.example.pekseries.model.Show
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class SeriesRepository {
    private val api = NetworkClient.api
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getTodayEpisodes(): List<Show> {
        val today = LocalDate.now().toString()

        try {
            val apiEpisodes = api.getSchedule(date = today)
            val watchedIds = getWatchedEpisodeIdsFromFirebase()

            return apiEpisodes.map { dto ->
                Show(
                    id = dto.show.id.toString(),
                    title = dto.show.name,
                    episode = "S${dto.season} • E${dto.number}",
                    time = dto.airtime,
                    imageUrl = dto.show.image?.medium ?: "",
                    isNew = true,
                    isWatched = watchedIds.contains(dto.id.toString())
                )
            }
        } catch (e: Exception) {
            Log.e("PekRepo", "Error fetching episodes", e)
            return emptyList()
        }
    }

    suspend fun getUpcomingSubscribedEpisodes(): List<Show> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val today = java.time.LocalDate.now().toString()

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .get()
                .await()
            val showIds = snapshot.documents.map { it.id }

            val upcomingShows = showIds.mapNotNull { id ->
                try {
                    val showDto = api.getShowById(id)
                    val episodes = api.getShowEpisodes(id)

                    val nextEpisode = episodes.firstOrNull { it.airdate != null && it.airdate >= today }

                    if (nextEpisode != null) {
                        Show(
                            id = id,
                            title = showDto.name,
                            imageUrl = showDto.image?.medium ?: "",
                            episode = "S${nextEpisode.season} • E${nextEpisode.number} - ${nextEpisode.name}",
                            time = nextEpisode.airdate ?: "",
                            isNew = false,
                            isWatched = false
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            upcomingShows.sortedBy { it.time }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getWatchedEpisodeIdsFromFirebase(): Set<String> {
        val userId = auth.currentUser?.uid ?: return emptySet()

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("watched_episodes")
                .get()
                .await()

            snapshot.documents.map { it.id }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

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
                .set(data)
                .await()
        } catch (e: Exception) {
            Log.e("PekRepo", "Failed to save to Firebase", e)
        }
    }

    suspend fun searchSeries(query: String): List<SearchResponseItem> {
        return NetworkClient.api.searchSeries(query)
    }

    suspend fun searchShows(query: String): List<Show> {
        return try {
            val searchResults = api.searchSeries(query)

            searchResults.map { item ->
                Show(
                    id = item.show.id.toString(),
                    title = item.show.name,
                    imageUrl = item.show.image?.medium ?: "",
                    episode = item.show.genres?.joinToString(", ") ?: "Search Result",
                    time = item.show.premiered?.let { "Premiered: $it" } ?: "",
                    isNew = false,
                    isWatched = false
                )
            }
        } catch (e: Exception) {
            Log.e("PekRepo", "Filter error", e)
            emptyList()
        }
    }

    suspend fun getShowEpisodes(showId: String): List<Episode> {
        return try {
            val episodes = api.getShowEpisodes(showId)
            episodes.reversed()
        } catch (e: Exception) {
            Log.e("PekRepo", "Error fetching show episodes", e)
            emptyList()
        }
    }

    suspend fun isSubscribed(showId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            val doc = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(showId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleSubscription(showId: String): Boolean {
        val userId = auth.currentUser?.uid
        Log.d("PekRepo", "Нажали подписку! showId: $showId, userId: $userId")

        if (userId == null) {
            Log.e("PekRepo", "ОШИБКА: Пользователь не авторизован (userId = null)!")
            return false
        }

        val docRef = db.collection( "users")
            .document(userId)
            .collection("subscriptions")
            .document(showId)

        return try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                Log.d("PekRepo", "Сериал найден в базе, удаляем...")
                docRef.delete().await()
                Log.d("PekRepo", "Успешно удалено!")
                false
            } else {
                Log.d("PekRepo", "Сериала нет в базе, добавляем...")
                val data = hashMapOf(
                    "showId" to showId,
                    "addedAt" to System.currentTimeMillis()
                )
                docRef.set(data).await()
                Log.d("PekRepo", "Успешно добавлено!")
                true
            }
        } catch (e: Exception) {
            Log.e("PekRepo", "ОШИБКА FIREBASE при записи/чтении", e)
            false
        }
    }
    suspend fun getSubscribedShows(): List<Show> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .get()
                .await()

            val showIds = snapshot.documents.map { it.id }

            val shows = showIds.mapNotNull { id ->
                try {
                    val dto = api.getShowById(id)
                    Show(
                        id = dto.id.toString(),
                        title = dto.name,
                        imageUrl = dto.image?.medium ?: "",
                        episode = "Subscribed",
                        time = "",
                        isNew = false,
                        isWatched = false
                    )
                } catch (e: Exception) {
                    null
                }
            }
            shows
        } catch (e: Exception) {
            Log.e("PekRepo", "Ошибка при загрузке подписок", e)
            emptyList()
        }
    }
    // ПОПУЛЯРНОЕ СЕГОДНЯ (Сортируем по weight)
    suspend fun getPopularToday(): List<Show> {
        val today = java.time.LocalDate.now().toString()
        return try {
            val apiEpisodes = api.getSchedule(date = today)
            val watchedIds = getWatchedEpisodeIdsFromFirebase()

            // Сортируем по убыванию популярности (weight)
            apiEpisodes.sortedByDescending { it.show.weight ?: 0 }.map { dto ->
                Show(
                    id = dto.show.id.toString(),
                    title = dto.show.name,
                    episode = "S${dto.season} • E${dto.number}",
                    time = dto.airtime,
                    imageUrl = dto.show.image?.medium ?: "",
                    isNew = true,
                    isWatched = watchedIds.contains(dto.id.toString())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // БУДУЩИЕ РЕЛИЗЫ (Завтрашние премьеры)
    suspend fun getUpcomingPremieres(): List<Show> {
        val tomorrow = java.time.LocalDate.now().plusDays(1).toString()
        return try {
            val apiEpisodes = api.getSchedule(date = tomorrow)
            val watchedIds = getWatchedEpisodeIdsFromFirebase()

            apiEpisodes.map { dto ->
                Show(
                    id = dto.show.id.toString(),
                    title = dto.show.name,
                    episode = "S${dto.season} • E${dto.number}",
                    time = "Tomorrow, ${dto.airtime}",
                    imageUrl = dto.show.image?.medium ?: "",
                    isNew = false,
                    isWatched = watchedIds.contains(dto.id.toString())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // АКТУАЛЬНОЕ (Вчерашние релизы)
    suspend fun getRecentEpisodes(): List<Show> {
        val yesterday = java.time.LocalDate.now().minusDays(1).toString()
        return try {
            val apiEpisodes = api.getSchedule(date = yesterday)
            val watchedIds = getWatchedEpisodeIdsFromFirebase()

            apiEpisodes.map { dto ->
                Show(
                    id = dto.show.id.toString(),
                    title = dto.show.name,
                    episode = "S${dto.season} • E${dto.number}",
                    time = "Yesterday",
                    imageUrl = dto.show.image?.medium ?: "",
                    isNew = false,
                    isWatched = watchedIds.contains(dto.id.toString())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
