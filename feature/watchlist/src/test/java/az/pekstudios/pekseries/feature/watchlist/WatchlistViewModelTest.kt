package az.pekstudios.pekseries.feature.watchlist

import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.testing.repository.FakeSubscriptionRepository
import az.pekstudios.pekseries.core.testing.repository.TestData
import az.pekstudios.pekseries.core.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class WatchlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSubscriptionRepository()

    @Test
    fun `loads upcoming and subscriptions on construction`() = runTest {
        repository.upcomingResult = PekResult.Success(TestData.shows(2))
        repository.subscribedShowsResult = PekResult.Success(TestData.shows(3))

        val state = WatchlistViewModel(repository).uiState.value

        assertThat(state.upcoming).hasSize(2)
        assertThat(state.subscriptions).hasSize(3)
        assertThat(state.isInitialLoading).isFalse()
        assertThat(state.error).isNull()
    }

    /**
     * Previously loadData caught into an empty block, so a failure left the old
     * lists on screen and the user was never told anything had gone wrong.
     */
    @Test
    fun `a failure is surfaced rather than swallowed`() = runTest {
        repository.upcomingResult = PekResult.Failure(DataError.Network)
        repository.subscribedShowsResult = PekResult.Failure(DataError.Network)

        val state = WatchlistViewModel(repository).uiState.value

        assertThat(state.error).isEqualTo(DataError.Network)
        assertThat(state.isInitialLoading).isFalse()
    }

    @Test
    fun `a partial failure keeps the half that loaded`() = runTest {
        repository.upcomingResult = PekResult.Success(TestData.shows(2))
        repository.subscribedShowsResult = PekResult.Failure(DataError.RateLimited)

        val state = WatchlistViewModel(repository).uiState.value

        assertThat(state.upcoming).hasSize(2)
        assertThat(state.error).isEqualTo(DataError.RateLimited)
    }

    @Test
    fun `signed out reports Unauthenticated instead of an empty watchlist`() = runTest {
        repository.upcomingResult = PekResult.Failure(DataError.Unauthenticated)
        repository.subscribedShowsResult = PekResult.Failure(DataError.Unauthenticated)

        val state = WatchlistViewModel(repository).uiState.value

        assertThat(state.error).isEqualTo(DataError.Unauthenticated)
        assertThat(state.isEmpty).isTrue()
    }

    @Test
    fun `retry clears a previous error once the call succeeds`() = runTest {
        repository.upcomingResult = PekResult.Failure(DataError.Network)
        repository.subscribedShowsResult = PekResult.Failure(DataError.Network)
        val vm = WatchlistViewModel(repository)
        assertThat(vm.uiState.value.error).isNotNull()

        repository.upcomingResult = PekResult.Success(TestData.shows(1))
        repository.subscribedShowsResult = PekResult.Success(TestData.shows(1))
        vm.retry()

        assertThat(vm.uiState.value.error).isNull()
        assertThat(vm.uiState.value.isRefreshing).isFalse()
    }

    @Test
    fun `isEmpty reflects both lists`() = runTest {
        repository.upcomingResult = PekResult.Success(emptyList())
        repository.subscribedShowsResult = PekResult.Success(emptyList())

        assertThat(WatchlistViewModel(repository).uiState.value.isEmpty).isTrue()
    }
}
