package com.example.pekseries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.data.repository.SeriesRepository
import com.example.pekseries.model.Show
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: SeriesRepository
) : ViewModel() {

    private val _subscriptions = MutableStateFlow<List<Show>>(emptyList())
    val subscriptions: StateFlow<List<Show>> = _subscriptions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayEpisodes = MutableStateFlow<List<Show>>(emptyList())
    val todayEpisodes: StateFlow<List<Show>> = _todayEpisodes.asStateFlow()

    private val _profileStats = MutableStateFlow(Triple(0, 0, 0))
    val profileStats: StateFlow<Triple<Int, Int, Int>> = _profileStats.asStateFlow()

    init {
        loadTodayEpisodes()
    }

    fun loadTodayEpisodes() {
        viewModelScope.launch {
            _isLoading.value = true
            _todayEpisodes.value = repository.getUpcomingSubscribedEpisodes()
            _isLoading.value = false
        }
    }

    fun loadSubscriptions() {
        viewModelScope.launch {
            _isLoading.value = true
            _subscriptions.value = repository.getSubscribedShows()
            _isLoading.value = false
        }
    }

    fun loadProfileStats() {
        viewModelScope.launch {
            val subs = repository.getSubscribedShows()
            if (subs.isEmpty()) {
                _profileStats.value = Triple(0, 0, 0)
                return@launch
            }

            val results = subs.map { show ->
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

            val totalEpisodes = results.sumOf { it.first }
            val totalMinutes = results.sumOf { it.second }

            _profileStats.value = Triple(subs.size, totalEpisodes, totalMinutes / 60)
        }
    }
}