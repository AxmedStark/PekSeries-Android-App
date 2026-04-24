package com.example.pekseries.data.repository

import android.util.Log
import com.example.pekseries.BuildConfig
import com.example.pekseries.data.NetworkClient
import com.example.pekseries.data.remote.TmdbShowDetailDto
import com.example.pekseries.data.remote.TmdbShowDto
import com.example.pekseries.model.Episode
import com.example.pekseries.model.Show
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SeriesRepository {
    private val tmdbApi = NetworkClient.tmdbApi
    private val tvMazeApi = NetworkClient.tvMazeApi
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Ключ берется безопасно из local.properties
    private val API_KEY = BuildConfig.TMDB_API_KEY

    // ==========================================
    // 1. TMDB: ГЛАВНАЯ СТРАНИЦА И ПОИСК
    // ==========================================

    private suspend fun mapTmdbToShow(dto: TmdbShowDto, isNew: Boolean = false): Show {
        val watchedIds = getWatchedEpisodeIdsFromFirebase()
        return Show(
            id = dto.id.toString(),
            title = dto.name,
            episode = dto.first_air_date?.let { "Премьера: $it" } ?: "TMDB",
            time = dto.vote_average?.let { "Рейтинг: ★ $it" } ?: "",
            imageUrl = dto.getFullPosterUrl(),
            isNew = isNew,
            isWatched = watchedIds.contains(dto.id.toString())
        )
    }

    suspend fun getTodayEpisodes(): List<Show> {
        return try {
            val response = tmdbApi.getAiringToday(API_KEY)
            response.results.map { mapTmdbToShow(it, isNew = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPopularToday(): List<Show> {
        return try {
            val response = tmdbApi.getTrending(API_KEY)
            response.results.map { mapTmdbToShow(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUpcomingPremieres(): List<Show> {
        return try {
            val response = tmdbApi.getOnTheAir(API_KEY)
            response.results.map { mapTmdbToShow(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun discoverShows(genreId: String?, year: String?, typeId: String?): List<Show> {
        return try {
            val response = tmdbApi.discoverShows(
                apiKey = API_KEY,
                genreIds = genreId,
                year = year,
                type = typeId
            )
            response.results.map { mapTmdbToShow(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchShows(query: String): List<Show> {
        return try {
            val response = tmdbApi.searchSeries(API_KEY, query)
            response.results.map { mapTmdbToShow(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==========================================
    // 2. DETAIL SCREEN И МОСТ (TMDB -> TVMAZE)
    // ==========================================

    suspend fun getShowDetails(tmdbId: String): TmdbShowDetailDto? {
        return try {
            tmdbApi.getShowDetails(tmdbId, API_KEY)
        } catch (e: Exception) {
            null
        }
    }

    // Тот самый мост!
    suspend fun getTvMazeId(tmdbId: String): String? {
        return try {
            val tmdbDetails = tmdbApi.getShowDetails(tmdbId, API_KEY)
            val imdbId = tmdbDetails.external_ids?.imdb_id

            if (!imdbId.isNullOrEmpty()) {
                val tvMazeShow = tvMazeApi.getTvMazeShowByImdb(imdbId)
                tvMazeShow.id.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("PekRepo", "Ошибка моста API", e)
            null
        }
    }

    suspend fun getShowEpisodes(tvMazeId: String): List<Episode> {
        return try {
            val episodes = tvMazeApi.getShowEpisodes(tvMazeId)
            episodes.reversed() // Разворачиваем, чтобы новые серии были сверху
        } catch (e: Exception) {
            Log.e("PekRepo", "Ошибка загрузки серий TVMaze", e)
            emptyList()
        }
    }

    // ==========================================
    // 3. FIREBASE И ПОДПИСКИ (Работают по TVMaze ID)
    // ==========================================

    private suspend fun getWatchedEpisodeIdsFromFirebase(): Set<String> {
        val userId = auth.currentUser?.uid ?: return emptySet()
        return try {
            val snapshot = db.collection("users").document(userId).collection("watched_episodes").get().await()
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
            db.collection("users").document(userId).collection("watched_episodes").document(episodeId).set(data).await()
        } catch (e: Exception) {}
    }

    suspend fun isSubscribed(tvMazeId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            val doc = db.collection("users").document(userId).collection("subscriptions").document(tvMazeId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleSubscription(tvMazeId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val docRef = db.collection("users").document(userId).collection("subscriptions").document(tvMazeId)
        return try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.delete().await()
                false
            } else {
                val data = hashMapOf(
                    "showId" to tvMazeId,
                    "addedAt" to System.currentTimeMillis()
                )
                docRef.set(data).await()
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSubscribedShows(): List<Show> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = db.collection("users").document(userId).collection("subscriptions").get().await()
            val showIds = snapshot.documents.map { it.id }

            showIds.mapNotNull { id ->
                try {
                    val dto = tvMazeApi.getShowById(id)
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
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUpcomingSubscribedEpisodes(): List<Show> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val today = java.time.LocalDate.now().toString()

        return try {
            val snapshot = db.collection("users").document(userId).collection("subscriptions").get().await()
            val showIds = snapshot.documents.map { it.id }

            val upcomingShows = showIds.mapNotNull { id ->
                try {
                    val showDto = tvMazeApi.getShowById(id)
                    val episodes = tvMazeApi.getShowEpisodes(id)
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
}