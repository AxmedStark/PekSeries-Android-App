package az.pekstudios.pekseries.core.data.mapper

import az.pekstudios.pekseries.core.network.remote.TmdbExternalIdsDto
import az.pekstudios.pekseries.core.network.remote.TmdbGenreDto
import az.pekstudios.pekseries.core.network.remote.TmdbShowDetailDto
import az.pekstudios.pekseries.core.network.remote.TmdbShowDto
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShowMappersTest {

    @Test
    fun `premiere dates are reordered to day-month-year`() {
        assertThat(formatPremiereDate("2024-03-17")).isEqualTo("17-03-2024")
    }

    /**
     * TMDB sometimes returns a bare year or an empty string. The original
     * implementation indexed parts[2] inside a try/catch; this asserts the
     * input survives rather than being silently blanked.
     */
    @Test
    fun `an unparseable premiere date is passed through unchanged`() {
        assertThat(formatPremiereDate("2024")).isEqualTo("2024")
        assertThat(formatPremiereDate("")).isEmpty()
    }

    @Test
    fun `ratings are formatted to one decimal with a US locale`() {
        // Hardcoded Locale.US matters: a comma decimal separator would render
        // "8,5" for users in most of Europe.
        assertThat(formatRating(8.46)).isEqualTo("Rating: ★ 8.5")
        assertThat(formatRating(null)).isEmpty()
    }

    @Test
    fun `a TMDB show maps onto the domain model`() {
        val dto = TmdbShowDto(
            id = 99,
            name = "Severance",
            poster_path = "/poster.jpg",
            backdrop_path = null,
            first_air_date = "2022-02-18",
            vote_average = 8.7,
            genre_ids = listOf(18),
        )

        val show = dto.toShow(isNew = true, isSubscribed = true)

        assertThat(show.id).isEqualTo("99")
        assertThat(show.title).isEqualTo("Severance")
        assertThat(show.episode).isEqualTo("Premiere: 18-02-2022")
        assertThat(show.time).isEqualTo("Rating: ★ 8.7")
        assertThat(show.imageUrl).endsWith("/poster.jpg")
        assertThat(show.isNew).isTrue()
        assertThat(show.isSubscribed).isTrue()
    }

    @Test
    fun `a TMDB show with no air date falls back to a label instead of showing null`() {
        val dto = TmdbShowDto(
            id = 1,
            name = "Unknown",
            poster_path = null,
            backdrop_path = null,
            first_air_date = null,
            vote_average = null,
            genre_ids = null,
        )

        val show = dto.toShow(isNew = false, isSubscribed = false)

        assertThat(show.episode).isEqualTo("TMDB")
        assertThat(show.time).isEmpty()
        assertThat(show.imageUrl).isEmpty()
    }

    @Test
    fun `show details carry the external ids needed to reach TVMaze`() {
        val dto = TmdbShowDetailDto(
            id = 7,
            name = "Dark",
            overview = "Time travel.",
            poster_path = "/dark.jpg",
            backdrop_path = null,
            vote_average = 8.8,
            genres = listOf(TmdbGenreDto(18, "Drama"), TmdbGenreDto(9648, "Mystery")),
            videos = null,
            external_ids = TmdbExternalIdsDto(imdb_id = "tt5753856", tvdb_id = 70523),
        )

        val details = dto.toShowDetails()

        assertThat(details.id).isEqualTo("7")
        assertThat(details.genres).containsExactly("Drama", "Mystery").inOrder()
        assertThat(details.imdbId).isEqualTo("tt5753856")
        assertThat(details.tvdbId).isEqualTo(70523)
    }
}
