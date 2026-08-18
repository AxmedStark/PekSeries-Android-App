package az.pekstudios.pekseries.core.testing.repository

import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.ShowRepository
import az.pekstudios.pekseries.core.model.Episode
import az.pekstudios.pekseries.core.model.Show
import az.pekstudios.pekseries.core.model.ShowDetails

/**
 * Hand-written fake rather than a mock: the tests care about what the ViewModel
 * does with success/empty/failure, and a fake expresses those three cases
 * without a stubbing DSL in every test.
 */
class FakeShowRepository : ShowRepository {

    var airingTodayResult: PekResult<List<Show>> = PekResult.Success(emptyList())
    var trendingResult: PekResult<List<Show>> = PekResult.Success(emptyList())
    var upcomingResult: PekResult<List<Show>> = PekResult.Success(emptyList())
    var discoverResult: PekResult<List<Show>> = PekResult.Success(emptyList())
    var searchResult: PekResult<List<Show>> = PekResult.Success(emptyList())
    var detailsResult: PekResult<ShowDetails> = PekResult.Success(TestData.details())
    var episodesResult: PekResult<List<Episode>> = PekResult.Success(emptyList())
    var tvMazeIdResult: PekResult<String> = PekResult.Success("1")
    var tmdbIdResult: PekResult<String> = PekResult.Success("1")

    /** Per-show episode overrides, for the watch-stats tests. */
    val episodesById = mutableMapOf<String, List<Episode>>()

    var lastDiscoverArgs: Triple<String?, String?, String?>? = null
    val searchQueries = mutableListOf<String>()

    override suspend fun getAiringToday() = airingTodayResult

    override suspend fun getTrending() = trendingResult

    override suspend fun getUpcomingPremieres() = upcomingResult

    override suspend fun discover(
        genreId: String?,
        year: String?,
        typeId: String?,
    ): PekResult<List<Show>> {
        lastDiscoverArgs = Triple(genreId, year, typeId)
        return discoverResult
    }

    override suspend fun search(query: String): PekResult<List<Show>> {
        searchQueries += query
        return searchResult
    }

    override suspend fun getShowDetails(tmdbId: String) = detailsResult

    override suspend fun getEpisodes(tvMazeId: String): PekResult<List<Episode>> =
        episodesById[tvMazeId]?.let { PekResult.Success(it) } ?: episodesResult

    override suspend fun findTvMazeId(details: ShowDetails) = tvMazeIdResult

    override suspend fun findTmdbId(tvMazeId: String) = tmdbIdResult

    fun failEverythingWith(error: DataError) {
        val failure = PekResult.Failure(error)
        airingTodayResult = failure
        trendingResult = failure
        upcomingResult = failure
        discoverResult = failure
        searchResult = failure
        detailsResult = failure
        episodesResult = failure
    }
}
