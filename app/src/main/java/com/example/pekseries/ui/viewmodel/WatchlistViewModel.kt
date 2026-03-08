package com.example.pekseries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.data.repository.SeriesRepository
import com.example.pekseries.model.Show
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WatchlistViewModel : ViewModel() {
    private val repository = SeriesRepository()

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

    private fun loadTodayEpisodes() {
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
            var totalEpisodes = 0
            var totalMinutes = 0

            subs.map { show ->
                launch {
                    try {
                        val episodes = repository.getShowEpisodes(show.id)
                        totalEpisodes += episodes.size
                        totalMinutes += episodes.size * 45

                        _profileStats.value = Triple(subs.size, totalEpisodes, totalMinutes / 60)
                    } catch (e: Exception) {}
                }
            }
            if (subs.isEmpty()) {
                _profileStats.value = Triple(0, 0, 0)
            }
        }
    }
}