package az.pekstudios.pekseries.core.domain.usecase

import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.model.WatchStats
import az.pekstudios.pekseries.core.testing.repository.FakeShowRepository
import az.pekstudios.pekseries.core.testing.repository.FakeSubscriptionRepository
import az.pekstudios.pekseries.core.testing.repository.TestData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetWatchStatsUseCaseTest {

    private lateinit var subscriptions: FakeSubscriptionRepository
    private lateinit var shows: FakeShowRepository
    private lateinit var useCase: GetWatchStatsUseCase

    @Before
    fun setUp() {
        subscriptions = FakeSubscriptionRepository()
        shows = FakeShowRepository()
        useCase = GetWatchStatsUseCase(subscriptions, shows)
    }

    @Test
    fun `returns zeroes when nothing is subscribed`() = runTest {
        subscriptions.subscribedShowsResult = PekResult.Success(emptyList())

        val result = useCase()

        assertThat(result).isEqualTo(PekResult.Success(WatchStats.EMPTY))
    }

    @Test
    fun `sums real episode runtimes across every subscribed show`() = runTest {
        subscriptions.subscribedShowsResult = PekResult.Success(
            listOf(TestData.show(id = "tvmaze_1"), TestData.show(id = "tvmaze_2")),
        )
        shows.episodesById["1"] = listOf(TestData.episode("1", runtime = 30), TestData.episode("2", runtime = 30))
        shows.episodesById["2"] = listOf(TestData.episode("3", runtime = 60))

        val stats = (useCase() as PekResult.Success).data

        assertThat(stats.seriesCount).isEqualTo(2)
        assertThat(stats.episodeCount).isEqualTo(3)
        assertThat(stats.totalMinutes).isEqualTo(120)
        assertThat(stats.totalHours).isEqualTo(2)
    }

    @Test
    fun `strips the tvmaze prefix before asking for episodes`() = runTest {
        subscriptions.subscribedShowsResult = PekResult.Success(listOf(TestData.show(id = "tvmaze_42")))
        shows.episodesById["42"] = listOf(TestData.episode("1", runtime = 45))

        val stats = (useCase() as PekResult.Success).data

        // Would be 0 if the prefix leaked into the lookup key.
        assertThat(stats.episodeCount).isEqualTo(1)
    }

    /**
     * Guards the regression that motivated this use case: Watchlist used to
     * assume 45 minutes for every episode while Profile summed real runtimes,
     * so the same account showed different totals on different screens.
     */
    @Test
    fun `falls back to 45 minutes only for episodes missing a runtime`() = runTest {
        subscriptions.subscribedShowsResult = PekResult.Success(listOf(TestData.show(id = "tvmaze_1")))
        shows.episodesById["1"] = listOf(
            TestData.episode("1", runtime = 10),
            TestData.episode("2", runtime = null),
        )

        val stats = (useCase() as PekResult.Success).data

        assertThat(stats.totalMinutes).isEqualTo(55)
    }

    @Test
    fun `a failure fetching episodes does not sink the whole calculation`() = runTest {
        subscriptions.subscribedShowsResult = PekResult.Success(listOf(TestData.show(id = "tvmaze_1")))
        shows.episodesResult = PekResult.Failure(DataError.Network)

        val stats = (useCase() as PekResult.Success).data

        assertThat(stats.seriesCount).isEqualTo(1)
        assertThat(stats.episodeCount).isEqualTo(0)
    }

    @Test
    fun `propagates a failure to load subscriptions`() = runTest {
        subscriptions.subscribedShowsResult = PekResult.Failure(DataError.Unauthenticated)

        assertThat(useCase()).isEqualTo(PekResult.Failure(DataError.Unauthenticated))
    }
}
