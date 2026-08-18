package az.pekstudios.pekseries.feature.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.model.Show
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistUiState(
    val upcoming: List<Show> = emptyList(),
    val subscriptions: List<Show> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: DataError? = null,
) {
    val isEmpty: Boolean get() = upcoming.isEmpty() && subscriptions.isEmpty()
}

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshing = isRefresh,
                    isInitialLoading = !isRefresh && it.isEmpty,
                    error = null,
                )
            }

            val (upcoming, subscriptions) = coroutineScope {
                val upcomingDeferred = async { subscriptionRepository.getUpcomingEpisodes() }
                val subscriptionsDeferred = async { subscriptionRepository.getSubscribedShows() }
                upcomingDeferred.await() to subscriptionsDeferred.await()
            }

            // A failure is surfaced rather than silently leaving the previous
            // lists on screen, which is what the old empty-catch block did.
            val error = (upcoming as? PekResult.Failure)?.error
                ?: (subscriptions as? PekResult.Failure)?.error

            _uiState.update { current ->
                current.copy(
                    upcoming = (upcoming as? PekResult.Success)?.data ?: current.upcoming,
                    subscriptions = (subscriptions as? PekResult.Success)?.data ?: current.subscriptions,
                    isInitialLoading = false,
                    isRefreshing = false,
                    error = error,
                )
            }
        }
    }

    fun retry() = loadData(isRefresh = true)
}
