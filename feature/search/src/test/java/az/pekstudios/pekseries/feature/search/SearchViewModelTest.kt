package az.pekstudios.pekseries.feature.search

import app.cash.turbine.test
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.testing.repository.FakeShowRepository
import az.pekstudios.pekseries.core.testing.repository.TestData
import az.pekstudios.pekseries.core.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    // Standard (not Unconfined) so debounce's virtual clock can be driven.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeShowRepository()

    @Test
    fun `starts Idle and issues no request`() = runTest {
        val vm = SearchViewModel(repository)

        assertThat(vm.uiState.value).isEqualTo(SearchUiState.Idle)
        assertThat(repository.searchQueries).isEmpty()
    }

    @Test
    fun `a query shorter than three characters never reaches the network`() = runTest {
        val vm = SearchViewModel(repository)

        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(SearchUiState.Idle)

            vm.onQueryChange("br")
            advanceTimeBy(1_000)

            assertThat(repository.searchQueries).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a settled query produces results`() = runTest {
        repository.searchResult = PekResult.Success(TestData.shows(2))
        val vm = SearchViewModel(repository)

        vm.uiState.test {
            assertThat(awaitItem()).isEqualTo(SearchUiState.Idle)

            vm.onQueryChange("breaking bad")
            advanceTimeBy(1_000)

            val states = buildList {
                add(awaitItem())
                add(awaitItem())
            }
            assertThat(states.last()).isInstanceOf(SearchUiState.Success::class.java)
            assertThat(repository.searchQueries).containsExactly("breaking bad")
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * The bug this replaced: with a manual job-cancel plus delay(500), a slow
     * response for an earlier query could resolve after a newer one and
     * overwrite it. debounce + flatMapLatest means only the final query is sent.
     */
    @Test
    fun `rapid typing debounces to a single request for the final query`() = runTest {
        repository.searchResult = PekResult.Success(TestData.shows(1))
        val vm = SearchViewModel(repository)

        vm.uiState.test {
            awaitItem()

            vm.onQueryChange("bre")
            advanceTimeBy(100)
            vm.onQueryChange("brea")
            advanceTimeBy(100)
            vm.onQueryChange("breaking")
            advanceTimeBy(1_000)

            assertThat(repository.searchQueries).containsExactly("breaking")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no results reports Empty with the query that found nothing`() = runTest {
        repository.searchResult = PekResult.Success(emptyList())
        val vm = SearchViewModel(repository)

        vm.uiState.test {
            awaitItem()
            vm.onQueryChange("zzzzzz")
            advanceTimeBy(1_000)

            awaitItem() // Loading
            assertThat(awaitItem()).isEqualTo(SearchUiState.Empty("zzzzzz"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed search reports the typed error instead of an empty list`() = runTest {
        repository.searchResult = PekResult.Failure(DataError.RateLimited)
        val vm = SearchViewModel(repository)

        vm.uiState.test {
            awaitItem()
            vm.onQueryChange("anything")
            advanceTimeBy(1_000)

            awaitItem() // Loading
            assertThat(awaitItem()).isEqualTo(SearchUiState.Error(DataError.RateLimited))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `queries are trimmed before being sent`() = runTest {
        repository.searchResult = PekResult.Success(TestData.shows(1))
        val vm = SearchViewModel(repository)

        vm.uiState.test {
            awaitItem()
            vm.onQueryChange("  dark  ")
            advanceTimeBy(1_000)

            assertThat(repository.searchQueries).containsExactly("dark")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
