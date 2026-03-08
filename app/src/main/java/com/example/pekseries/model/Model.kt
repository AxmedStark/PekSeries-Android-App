package com.example.pekseries.model

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

data class Show(
    val id: String,
    @SerializedName("name")
    val title: String,
    val episode: String? = null,
    val time: String? = null,
    val image: TvMazeImage? = null,
    val imageUrl: String? = null,
    val isNew: Boolean = false,
    val isWatched: Boolean = false
) {
    fun getPosterUrl(): String {
        return image?.medium ?: imageUrl ?: ""
    }
}
data class Episode(
    val id: String,
    val name: String,
    val season: Int,
    val number: Int,
    val airdate: String,

    val videoUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
)
data class TvMazeImage(
    val medium: String?
)

data class TrendingItem(
    val title: String,
    val color: Color
)

data class TimelineItem(
    val time: String,
    val title: String,
    val sub: String,
    val isLive: Boolean = false
)

data class SearchResponseItem(
    val show: Show
)
