package az.pekstudios.pekseries.core.data.mapper

import az.pekstudios.pekseries.core.model.Show
import az.pekstudios.pekseries.core.model.ShowDetails
import az.pekstudios.pekseries.core.model.TvMazeShowDto
import az.pekstudios.pekseries.core.network.remote.TmdbShowDetailDto
import az.pekstudios.pekseries.core.network.remote.TmdbShowDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd-MM").withZone(ZoneId.systemDefault())

/** "2024-03-17" -> "17-03-2024". Returns the input unchanged if it is not a date. */
internal fun formatPremiereDate(raw: String): String {
    val parts = raw.split("-")
    return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else raw
}

internal fun formatRating(value: Double?): String =
    value?.let { String.format(Locale.US, "Rating: ★ %.1f", it) } ?: ""

internal fun TmdbShowDto.toShow(isNew: Boolean, isSubscribed: Boolean): Show = Show(
    id = id.toString(),
    title = name,
    episode = first_air_date?.let { "Premiere: ${formatPremiereDate(it)}" } ?: "TMDB",
    time = formatRating(vote_average),
    imageUrl = getFullPosterUrl(),
    isNew = isNew,
    isSubscribed = isSubscribed,
)

internal fun TvMazeShowDto.toShow(isSubscribed: Boolean): Show = Show(
    id = "$TVMAZE_ID_PREFIX$id",
    title = name,
    imageUrl = image?.medium.orEmpty(),
    episode = premiered?.let { "Premiere: ${formatPremiereDate(it)}" } ?: "TV Show",
    time = formatRating(rating?.average),
    isSubscribed = isSubscribed,
)

internal fun TmdbShowDetailDto.toShowDetails(): ShowDetails = ShowDetails(
    id = id.toString(),
    title = name,
    overview = overview,
    rating = vote_average,
    genres = genres?.map { it.name }.orEmpty(),
    posterUrl = getFullPosterUrl(),
    imdbId = external_ids?.imdb_id,
    tvdbId = external_ids?.tvdb_id,
)

/** A subscribed show rendered as its next upcoming episode. */
internal fun TvMazeShowDto.toUpcomingShow(
    season: Int,
    number: Int,
    episodeName: String,
    airstamp: Instant,
): Show = Show(
    id = "$TVMAZE_ID_PREFIX$id",
    title = name,
    imageUrl = image?.medium.orEmpty(),
    episode = "S$season E$number - $episodeName",
    time = TIME_FORMATTER.format(airstamp),
    dateDisplay = DATE_FORMATTER.format(airstamp),
    isSubscribed = true,
    airTimeMs = airstamp.toEpochMilli(),
)

internal const val TVMAZE_ID_PREFIX = "tvmaze_"
