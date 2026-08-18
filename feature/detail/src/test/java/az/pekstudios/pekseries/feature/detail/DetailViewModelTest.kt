package az.pekstudios.pekseries.feature.detail

import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.testing.repository.FakeShowRepository
import az.pekstudios.pekseries.core.testing.repository.FakeSubscriptionRepository
import az.pekstudios.pekseries.core.testing.repository.TestData
import az.pekstudios.pekseries.core.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val shows = FakeShowRepository()
    private val subscriptions = FakeSubscriptionRepository()

    private fun viewModel() = DetailViewModel(shows, subscriptions)

    @Test
    fun `a TMDB id loads details and episodes`() = runTest {
        shows.detailsResult = PekResult.Success(TestData.details(title = "Dark"))
        shows.episodesResult = PekResult.Success(listOf(TestData.episode()))
        val vm = viewModel()

        vm.loadEpisodes("123")

        val state = vm.uiState.value
        assertThat(state.details?.title).isEqualTo("Dark")
        assertThat(state.episodes).hasSize(1)
        assertThat(state.canSubscribe).isTrue()
        assertThat(state.isLoading).isFalse()
    }

    /** A push carries the TVMaze id, so it has to be translated first. */
    @Test
    fun `a tvmaze-prefixed id is resolved to a TMDB id first`() = runTest {
        shows.tmdbIdResult = PekResult.Success("555")
        shows.detailsResult = PekResult.Success(TestData.details(title = "From Push"))
        val vm = viewModel()

        vm.loadEpisodes("tvmaze_42")

        assertThat(vm.uiState.value.details?.title).isEqualTo("From Push")
    }

    @Test
    fun `an unresolvable id reports NotFound rather than spinning forever`() = runTest {
        shows.tmdbIdResult = PekResult.Failure(DataError.NotFound)
        val vm = viewModel()

        vm.loadEpisodes("tvmaze_unknown")

        assertThat(vm.uiState.value.isLoading).isFalse()
        assertThat(vm.uiState.value.error).isEqualTo(DataError.NotFound)
    }

    @Test
    fun `a failure loading details stops the load and reports the cause`() = runTest {
        shows.detailsResult = PekResult.Failure(DataError.Network)
        val vm = viewModel()

        vm.loadEpisodes("123")

        assertThat(vm.uiState.value.error).isEqualTo(DataError.Network)
        assertThat(vm.uiState.value.isLoading).isFalse()
    }

    /** Details are still useful even when the show cannot be subscribed to. */
    @Test
    fun `details still render when no TVMaze id can be found`() = runTest {
        shows.detailsResult = PekResult.Success(TestData.details(title = "Obscure"))
        shows.tvMazeIdResult = PekResult.Failure(DataError.NotFound)
        val vm = viewModel()

        vm.loadEpisodes("123")

        assertThat(vm.uiState.value.details?.title).isEqualTo("Obscure")
        assertThat(vm.uiState.value.canSubscribe).isFalse()
        assertThat(vm.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `toggling subscription reflects the new state`() = runTest {
        shows.detailsResult = PekResult.Success(TestData.details())
        subscriptions.isSubscribedResult = PekResult.Success(false)
        subscriptions.toggleResult = PekResult.Success(true)
        val vm = viewModel()
        vm.loadEpisodes("123")

        vm.toggleSubscription()

        assertThat(vm.uiState.value.isSubscribed).isTrue()
    }

    @Test
    fun `a failed toggle reports the error and leaves the state alone`() = runTest {
        shows.detailsResult = PekResult.Success(TestData.details())
        subscriptions.isSubscribedResult = PekResult.Success(false)
        subscriptions.toggleResult = PekResult.Failure(DataError.Unauthenticated)
        val vm = viewModel()
        vm.loadEpisodes("123")

        vm.toggleSubscription()

        assertThat(vm.uiState.value.isSubscribed).isFalse()
        assertThat(vm.uiState.value.error).isEqualTo(DataError.Unauthenticated)
    }

    @Test
    fun `toggling before a show is loaded is a no-op`() = runTest {
        val vm = viewModel()

        vm.toggleSubscription()

        assertThat(vm.uiState.value.isSubscribed).isFalse()
    }
}
