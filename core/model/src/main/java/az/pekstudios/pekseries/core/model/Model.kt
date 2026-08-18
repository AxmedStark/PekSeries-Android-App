package az.pekstudios.pekseries.core.model

data class Show(
    val id: String,
    val title: String,
    val episode: String? = null,
    val time: String? = null,
    val dateDisplay: String? = null,
    val imageUrl: String? = null,
    val isNew: Boolean = false,
    val isWatched: Boolean = false,
    val isSubscribed: Boolean = false,
    val airTimeMs: Long = 0L
) {
    fun getPosterUrl(): String = imageUrl ?: ""
}

data class Episode(
    val id: String,
    val name: String,
    val season: Int,
    val number: Int,
    val airdate: String? = null,
    val airstamp: String? = null,
    val runtime: Int? = null
)

data class SearchResponseItem(
    val score: Double? = null,
    val show: TvMazeShowDto
)

data class TvMazeShowDto(
    val id: Int,
    val name: String,
    val image: TvMazeImageDto?,
    val summary: String? = null,
    val genres: List<String>? = null,
    val rating: TvMazeRatingDto? = null,
    val premiered: String? = null,
    val weight: Int? = 0,
    val type: String? = null,
    val externals: TvMazeExternalsDto? = null
)

data class TvMazeRatingDto(
    val average: Double?
)

data class TvMazeImageDto(
    val medium: String?,
    val original: String?
)

data class TvMazeExternalsDto(val imdb: String?, val thetvdb: Int?)

data class TvMazeEpisodeDto(
    val id: Int,
    val name: String,
    val season: Int,
    val number: Int,
    val airdate: String?,
    val airtime: String?,
    val airstamp: String?,
    val runtime: Int? = null,
    val show: TvMazeShowDto
)

/**
 * Show metadata for the detail screen, mapped from the TMDB response so that no
 * wire DTO reaches the UI.
 */
data class ShowDetails(
    val id: String,
    val title: String,
    val overview: String?,
    val rating: Double?,
    val genres: List<String> = emptyList(),
    val posterUrl: String? = null,
    val imdbId: String? = null,
    val tvdbId: Int? = null,
)

data class WatchStats(
    val seriesCount: Int = 0,
    val episodeCount: Int = 0,
    val totalMinutes: Int = 0,
) {
    val totalHours: Int get() = totalMinutes / 60

    companion object {
        val EMPTY = WatchStats()
    }
}

/**
 * What the profile screen renders: provider identity with any local override
 * already applied, so the UI never has to decide which value wins.
 */
data class UserProfile(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val pushEnabled: Boolean = true,
    val isSignedIn: Boolean = false,
    val hasCustomDisplayName: Boolean = false,
    val hasCustomPhoto: Boolean = false,
)
