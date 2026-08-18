package az.pekstudios.pekseries.core.domain.usecase

import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.getOrDefault
import az.pekstudios.pekseries.core.domain.map
import az.pekstudios.pekseries.core.domain.repository.ShowRepository
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.model.WatchStats
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Totals shown on the profile screen.
 *
 * This existed twice before, and the two copies had already drifted:
 * ProfileViewModel summed each episode's real `runtime`, while WatchlistViewModel
 * multiplied the episode count by a hardcoded 45 minutes, so the same user saw
 * two different "hours watched" figures depending on the screen. One
 * implementation, used by both.
 */
class GetWatchStatsUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val showRepository: ShowRepository,
) {
    suspend operator fun invoke(): PekResult<WatchStats> =
        subscriptionRepository.getSubscribedShows().map { shows ->
            if (shows.isEmpty()) return@map WatchStats.EMPTY

            val perShow = coroutineScope {
                shows.map { show ->
                    async {
                        val episodes = showRepository
                            .getEpisodes(show.id.removePrefix(TVMAZE_ID_PREFIX))
                            .getOrDefault(emptyList())

                        // Fall back to 45 minutes only when the API omits a
                        // runtime, rather than for every episode.
                        episodes.size to episodes.sumOf { it.runtime ?: DEFAULT_RUNTIME_MINUTES }
                    }
                }.awaitAll()
            }

            WatchStats(
                seriesCount = shows.size,
                episodeCount = perShow.sumOf { it.first },
                totalMinutes = perShow.sumOf { it.second },
            )
        }

    private companion object {
        const val TVMAZE_ID_PREFIX = "tvmaze_"
        const val DEFAULT_RUNTIME_MINUTES = 45
    }
}
