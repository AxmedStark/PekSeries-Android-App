package com.example.pekseries.data.repository

import com.example.pekseries.BuildConfig
import com.example.pekseries.data.NetworkClient
import com.example.pekseries.data.remote.TmdbShowDetailDto
import com.example.pekseries.data.remote.TmdbShowDto
import com.example.pekseries.model.Episode
import com.example.pekseries.model.Show
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Locale

class SeriesRepository {
    private val tmdbApi = NetworkClient.tmdbApi
    private val tvMazeApi = NetworkClient.tvMazeApi
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val API_KEY = BuildConfig.TMDB_API_KEY

    private suspend fun filterAndMapTmdbList(dtos: List<TmdbShowDto>, isNew: Boolean = false): List<Show> {
        return coroutineScope {
            dtos.take(15).map { dto ->
                async {
                    val details = getShowDetails(dto.id.toString())
                    val mazeId = findTvMazeId(details)

                    if (mazeId != null) {
                        val watchedIds = getWatchedEpisodeIdsFromFirebase()
                        Show(
                            id = dto.id.toString(),
                            title = dto.name,
                            episode = dto.first_air_date?.let { "Released: $it" } ?: "TMDB",
                            time = dto.vote_average?.let { String.format(Locale.US, "Rating: ★ %.1f", it) } ?: "",
                            imageUrl = dto.getFullPosterUrl(),
                            isNew = isNew,
                            isWatched = watchedIds.contains(mazeId)
                        )
                    } else {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    suspend fun getTodayEpisodes(): List<Show> {
        return try {
            val response = tmdbApi.getAiringToday(API_KEY)
            filterAndMapTmdbList(response.results, isNew = true)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getPopularToday(): List<Show> {
        return try {
            val response = tmdbApi.getTrending(API_KEY)
            filterAndMapTmdbList(response.results)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getUpcomingPremieres(): List<Show> {
        return try {
            val response = tmdbApi.getOnTheAir(API_KEY)
            filterAndMapTmdbList(response.results)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun discoverShows(genreId: String?, year: String?, typeId: String?): List<Show> {
        return try {
            val response = tmdbApi.discoverShows(apiKey = API_KEY, genreIds = genreId, year = year, type = typeId)
            filterAndMapTmdbList(response.results)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun searchSeriesTvMaze(query: String): List<Show> {
        return try {
            val results = tvMazeApi.searchSeries(query)
            results.map { item ->
                Show(
                    id = "tvmaze_${item.show.id}",
                    title = item.show.name,
                    imageUrl = item.show.image?.medium ?: "",
                    episode = item.show.genres?.joinToString(", ") ?: "TV Show",
                    time = item.show.premiered?.let { "Premiere: $it" } ?: "",
                    isNew = false,
                    isWatched = false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getShowDetails(tmdbId: String): TmdbShowDetailDto? {
        return try {
            tmdbApi.getShowDetails(tmdbId, API_KEY)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun findTvMazeId(tmdbDetails: TmdbShowDetailDto?): String? {
        if (tmdbDetails == null) return null

        val imdbId = tmdbDetails.external_ids?.imdb_id
        val tvdbId = tmdbDetails.external_ids?.tvdb_id
        val showName = tmdbDetails.name

        if (!imdbId.isNullOrEmpty()) {
            try {
                val tvMazeShow = tvMazeApi.getTvMazeShowByImdb(imdbId)
                return tvMazeShow.id.toString()
            } catch (e: Exception) { }
        }

        if (tvdbId != null) {
            try {
                val tvMazeShow = tvMazeApi.getTvMazeShowByTvdb(tvdbId)
                return tvMazeShow.id.toString()
            } catch (e: Exception) { }
        }

        try {
            val searchResults = tvMazeApi.searchSeries(showName)
            val match = searchResults.firstOrNull {
                it.show.name.equals(showName, ignoreCase = true)
            }
            if (match != null) {
                return match.show.id.toString()
            }
        } catch (e: Exception) { }

        return null
    }

    suspend fun getTmdbIdByTvMazeId(tvMazeId: String): String? {
        return try {
            val tvMazeShow = tvMazeApi.getShowById(tvMazeId)
            val imdbId = tvMazeShow.externals?.imdb
            if (!imdbId.isNullOrEmpty()) {
                val findResponse = tmdbApi.findByExternalId(imdbId, API_KEY)
                findResponse.tv_results.firstOrNull()?.id?.toString()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getShowEpisodes(tvMazeId: String): List<Episode> {
        return try {
            val episodes = tvMazeApi.getShowEpisodes(tvMazeId)
            episodes.reversed()
        } catch (e: Exception) {
            emptyList()
        }
    }

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
                        id = "tvmaze_${dto.id}",
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
                            id = "tvmaze_$id",
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

    data class PekNotification(
        val id: String = "",
        val title: String = "",
        val message: String = "",
        val timestamp: Long = 0
    )

    suspend fun saveNotification(title: String, message: String) {
        val userId = auth.currentUser?.uid ?: return
        val notification = PekNotification(
            title = title,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        try {
            db.collection("users").document(userId).collection("notifications").add(notification).await()
        } catch (e: Exception) {}
    }

    suspend fun getNotifications(): List<PekNotification> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(PekNotification::class.java)
        } catch (e: Exception) { emptyList() }
    }
}