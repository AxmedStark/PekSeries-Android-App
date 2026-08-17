package az.pekstudios.pekseries.feature.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.model.Show
import az.pekstudios.pekseries.core.network.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: SeriesRepository
) : ViewModel() {

    private val _subscriptions = MutableStateFlow<List<Show>>(emptyList())
    val subscriptions: StateFlow<List<Show>> = _subscriptions.asStateFlow()

    private val _todayEpisodes = MutableStateFlow<List<Show>>(emptyList())
    val todayEpisodes: StateFlow<List<Show>> = _todayEpisodes.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _profileStats = MutableStateFlow(Triple(0, 0, 0))
    val profileStats: StateFlow<Triple<Int, Int, Int>> = _profileStats.asStateFlow()

    init {
        loadData(isRefresh = false)
        loadProfileStats()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else if (_todayEpisodes.value.isEmpty() && _subscriptions.value.isEmpty()) {
                _isInitialLoading.value = true
            }

            try {
                coroutineScope {
                    val upcomingDeferred = async { repository.getUpcomingSubscribedEpisodes() }
                    val subscriptionsDeferred = async { repository.getSubscribedShows() }

                    _todayEpisodes.value = upcomingDeferred.await()
                    _subscriptions.value = subscriptionsDeferred.await()
                }
            } catch (e: Exception) {

            } finally {
                _isInitialLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    private fun loadProfileStats() {
        viewModelScope.launch {
            val subs = repository.getSubscribedShows()
            if (subs.isEmpty()) {
                _profileStats.value = Triple(0, 0, 0)
                return@launch
            }

            val results = coroutineScope {
                subs.map { show ->
                    async {
                        try {
                            val cleanId = show.id.removePrefix("tvmaze_")
                            val episodes = repository.getShowEpisodes(cleanId)
                            Pair(episodes.size, episodes.size * 45)
                        } catch (e: Exception) {
                            Pair(0, 0)
                        }
                    }
                }.awaitAll()
            }

            val totalEpisodes = results.sumOf { it.first }
            val totalMinutes = results.sumOf { it.second }

            _profileStats.value = Triple(subs.size, totalEpisodes, totalMinutes / 60)
        }
    }
}