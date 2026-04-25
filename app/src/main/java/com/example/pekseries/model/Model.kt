package com.example.pekseries.model

import com.example.pekseries.data.remote.TvMazeShowDto

data class Show(
    val id: String,
    val title: String,
    val episode: String? = null,
    val time: String? = null,
    val imageUrl: String? = null,
    val isNew: Boolean = false,
    val isWatched: Boolean = false
) {
    fun getPosterUrl(): String {
        return imageUrl ?: ""
    }
}

data class Episode(
    val id: String,
    val name: String,
    val season: Int,
    val number: Int,
    val airdate: String? = null,
    val airstamp: String? = null
)

data class SearchResponseItem(
    val score: Double? = null,
    val show: TvMazeShowDto
)