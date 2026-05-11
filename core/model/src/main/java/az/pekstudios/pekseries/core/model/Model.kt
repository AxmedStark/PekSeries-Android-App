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
    val airstamp: String? = null
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
    val show: TvMazeShowDto
)
