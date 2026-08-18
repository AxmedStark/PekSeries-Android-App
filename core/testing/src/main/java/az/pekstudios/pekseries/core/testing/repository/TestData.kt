package az.pekstudios.pekseries.core.testing.repository

import az.pekstudios.pekseries.core.model.Episode
import az.pekstudios.pekseries.core.model.Show
import az.pekstudios.pekseries.core.model.ShowDetails

object TestData {

    fun show(
        id: String = "1",
        title: String = "Test Show",
        isSubscribed: Boolean = false,
        airTimeMs: Long = 0L,
    ) = Show(
        id = id,
        title = title,
        episode = "S1 E1",
        time = "20:00",
        imageUrl = "https://example.test/poster.jpg",
        isSubscribed = isSubscribed,
        airTimeMs = airTimeMs,
    )

    fun shows(count: Int) = List(count) { show(id = "${it + 1}", title = "Show ${it + 1}") }

    fun episode(id: String = "1", runtime: Int? = 45) = Episode(
        id = id,
        name = "Episode $id",
        season = 1,
        number = id.toIntOrNull() ?: 1,
        airdate = "2026-01-01",
        airstamp = "2026-01-01T20:00:00+00:00",
        runtime = runtime,
    )

    fun details(id: String = "1", title: String = "Test Show") = ShowDetails(
        id = id,
        title = title,
        overview = "An overview.",
        rating = 8.5,
        genres = listOf("Drama"),
        posterUrl = "https://example.test/poster.jpg",
        imdbId = "tt0000001",
    )
}
