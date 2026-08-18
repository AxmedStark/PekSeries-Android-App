package az.pekstudios.pekseries.core.data.repository

import az.pekstudios.pekseries.core.data.mapper.toShow
import az.pekstudios.pekseries.core.data.mapper.toShowDetails
import az.pekstudios.pekseries.core.data.runCatchingData
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.getOrDefault
import az.pekstudios.pekseries.core.domain.getOrNull
import az.pekstudios.pekseries.core.domain.isSuccess
import az.pekstudios.pekseries.core.domain.orNotFound
import az.pekstudios.pekseries.core.domain.repository.ShowRepository
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.model.Episode
import az.pekstudios.pekseries.core.model.Show
import az.pekstudios.pekseries.core.model.ShowDetails
import az.pekstudios.pekseries.core.network.BuildConfig
import az.pekstudios.pekseries.core.network.remote.TmdbApi
import az.pekstudios.pekseries.core.network.remote.TmdbShowDto
import az.pekstudios.pekseries.core.network.remote.TvMazeApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val tvMazeApi: TvMazeApi,
    private val subscriptionRepository: SubscriptionRepository,
) : ShowRepository {

    private val apiKey = BuildConfig.TMDB_API_KEY

    override suspend fun getAiringToday(): PekResult<List<Show>> =
        runCatchingData("getAiringToday") { tmdbApi.getAiringToday(apiKey).results }
            .enrich(isNew = true)

    override suspend fun getTrending(): PekResult<List<Show>> =
        runCatchingData("getTrending") { tmdbApi.getTrending(apiKey).results }
            .enrich()

    override suspend fun getUpcomingPremieres(): PekResult<List<Show>> =
        runCatchingData("getUpcomingPremieres") { tmdbApi.getOnTheAir(apiKey).results }
            .enrich()

    override suspend fun discover(
        genreId: String?,
        year: String?,
        typeId: String?,
    ): PekResult<List<Show>> = runCatchingData("discover") {
        tmdbApi.discoverShows(
            apiKey = apiKey,
            genreIds = genreId,
            year = year,
            type = typeId,
        ).results
    }.enrich()

    override suspend fun search(query: String): PekResult<List<Show>> =
        runCatchingData("search") {
            val subscribedIds = subscriptionRepository.getSubscribedIds().getOrDefault(emptySet())
            tvMazeApi.searchSeries(query).map { item ->
                item.show.toShow(isSubscribed = subscribedIds.contains(item.show.id.toString()))
            }
        }

    override suspend fun getShowDetails(tmdbId: String): PekResult<ShowDetails> =
        runCatchingData("getShowDetails") { tmdbApi.getShowDetails(tmdbId, apiKey).toShowDetails() }

    override suspend fun getEpisodes(tvMazeId: String): PekResult<List<Episode>> =
        runCatchingData("getEpisodes") { tvMazeApi.getShowEpisodes(tvMazeId).reversed() }

    /**
     * Resolved by external id where available, falling back to a name search.
     * Each strategy is attempted separately because TVMaze answers a miss with
     * 404 rather than an empty body.
     */
    override suspend fun findTvMazeId(details: ShowDetails): PekResult<String> {
        details.imdbId?.takeIf(String::isNotEmpty)?.let { imdb ->
            val byImdb = runCatchingData("findTvMazeId(imdb)") {
                tvMazeApi.getTvMazeShowByImdb(imdb).id.toString()
            }
            if (byImdb.isSuccess) return byImdb
        }

        details.tvdbId?.let { tvdb ->
            val byTvdb = runCatchingData("findTvMazeId(tvdb)") {
                tvMazeApi.getTvMazeShowByTvdb(tvdb).id.toString()
            }
            if (byTvdb.isSuccess) return byTvdb
        }

        return runCatchingData("findTvMazeId(search)") {
            tvMazeApi.searchSeries(details.title)
                .firstOrNull { it.show.name.equals(details.title, ignoreCase = true) }
                ?.show?.id?.toString()
        }.orNotFound()
    }

    override suspend fun findTmdbId(tvMazeId: String): PekResult<String> =
        runCatchingData("findTmdbId") {
            val imdbId = tvMazeApi.getShowById(tvMazeId).externals?.imdb
            if (imdbId.isNullOrEmpty()) {
                null
            } else {
                tmdbApi.findByExternalId(imdbId, apiKey).tv_results.firstOrNull()?.id?.toString()
            }
        }.orNotFound()

    /**
     * A TMDB entry needs a TVMaze id before it can be subscribed to, which costs
     * extra calls per show, so the page is capped and resolved in parallel.
     *
     * TODO(perf): still N+1 — 15 shows can mean up to 60 upstream calls, and is
     *  the main source of TVMaze 429s. A persisted id cache would remove it.
     */
    private suspend fun PekResult<List<TmdbShowDto>>.enrich(
        isNew: Boolean = false,
    ): PekResult<List<Show>> {
        val dtos = when (this) {
            is PekResult.Failure -> return this
            is PekResult.Success -> data
        }

        val subscribedIds = subscriptionRepository.getSubscribedIds().getOrDefault(emptySet())

        val shows = coroutineScope {
            dtos.take(PAGE_LIMIT).map { dto ->
                async {
                    val details = getShowDetails(dto.id.toString()).getOrNull() ?: return@async null
                    val mazeId = findTvMazeId(details).getOrNull() ?: return@async null
                    dto.toShow(isNew = isNew, isSubscribed = subscribedIds.contains(mazeId))
                }
            }.awaitAll().filterNotNull()
        }

        return PekResult.Success(shows)
    }

    private companion object {
        const val PAGE_LIMIT = 15
    }
}
