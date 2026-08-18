package az.pekstudios.pekseries.feature.home

import app.cash.turbine.test
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.testing.repository.FakeShowRepository
import az.pekstudios.pekseries.core.testing.repository.FakeUserProfileRepository
import az.pekstudios.pekseries.core.testing.repository.TestData
import az.pekstudios.pekseries.core.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeShowRepository()
    private val profileRepository = FakeUserProfileRepository()

    private fun viewModel() = HomeViewModel(repository, profileRepository)

    @Test
    fun `loads airing today on construction`() = runTest {
        repository.airingTodayResult = PekResult.Success(TestData.shows(3))

        val state = viewModel().uiState.value

        assertThat(state).isInstanceOf(HomeUiState.Success::class.java)
        assertThat((state as HomeUiState.Success).shows).hasSize(3)
    }

    /**
     * The distinction that did not exist before: the repository used to return
     * an empty list for both cases, so the UI could only ever say "nothing here".
     */
    @Test
    fun `an empty result is Empty, not Success with an empty list`() = runTest {
        repository.airingTodayResult = PekResult.Success(emptyList())

        assertThat(viewModel().uiState.value).isEqualTo(HomeUiState.Empty)
    }

    @Test
    fun `a failure surfaces as Error carrying the typed cause`() = runTest {
        repository.airingTodayResult = PekResult.Failure(DataError.Network)

        val state = viewModel().uiState.value

        assertThat(state).isEqualTo(HomeUiState.Error(DataError.Network))
    }

    @Test
    fun `each category reads from its own endpoint`() = runTest {
        repository.trendingResult = PekResult.Success(listOf(TestData.show(title = "Trending")))
        repository.upcomingResult = PekResult.Success(listOf(TestData.show(title = "Upcoming")))
        val vm = viewModel()

        vm.loadEpisodes("Popular")
        assertThat((vm.uiState.value as HomeUiState.Success).shows.single().title).isEqualTo("Trending")

        vm.loadEpisodes("Upcoming")
        assertThat((vm.uiState.value as HomeUiState.Success).shows.single().title).isEqualTo("Upcoming")
    }

    @Test
    fun `an unrecognised category falls back to airing today`() = runTest {
        repository.airingTodayResult = PekResult.Success(listOf(TestData.show(title = "Today")))
        val vm = viewModel()

        vm.loadEpisodes("Nonsense")

        assertThat((vm.uiState.value as HomeUiState.Success).shows.single().title).isEqualTo("Today")
    }

    @Test
    fun `placeholder filter labels are sent as null rather than as literal text`() = runTest {
        repository.discoverResult = PekResult.Success(TestData.shows(1))
        val vm = viewModel()

        vm.applyFilters(genre = "Genre", type = "Type", year = "Year")

        assertThat(repository.lastDiscoverArgs).isEqualTo(Triple(null, null, null))
    }

    @Test
    fun `All is treated as no filter`() = runTest {
        repository.discoverResult = PekResult.Success(TestData.shows(1))
        val vm = viewModel()

        vm.applyFilters(genre = "All", type = "All", year = "All")

        assertThat(repository.lastDiscoverArgs).isEqualTo(Triple(null, null, null))
    }

    @Test
    fun `named filters are translated into TMDB ids`() = runTest {
        repository.discoverResult = PekResult.Success(TestData.shows(1))
        val vm = viewModel()

        vm.applyFilters(genre = "Comedy", type = "Miniseries", year = "2024")

        assertThat(repository.lastDiscoverArgs).isEqualTo(Triple("35", "2024", "2"))
    }

    @Test
    fun `the greeting follows the profile repository, not FirebaseAuth`() = runTest {
        profileRepository.profileFlow.value =
            profileRepository.profileFlow.value.copy(displayName = "Ahmed Stark")

        // profile is a WhileSubscribed StateFlow, so it only starts collecting
        // upstream once something observes it.
        viewModel().profile.test {
            assertThat(expectMostRecentItem().displayName).isEqualTo("Ahmed Stark")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry re-issues the request after a failure`() = runTest {
        repository.airingTodayResult = PekResult.Failure(DataError.Network)
        val vm = viewModel()
        assertThat(vm.uiState.value).isInstanceOf(HomeUiState.Error::class.java)

        repository.airingTodayResult = PekResult.Success(TestData.shows(2))
        vm.retry()

        assertThat(vm.uiState.value).isInstanceOf(HomeUiState.Success::class.java)
    }
}
