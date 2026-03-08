package com.example.pekseries.data.repository

import android.util.Log
import com.example.pekseries.data.NetworkClient
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
                    id = dto.id.toString(),
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
}
