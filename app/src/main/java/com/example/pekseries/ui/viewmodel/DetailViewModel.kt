package com.example.pekseries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.data.repository.SeriesRepository
import com.example.pekseries.model.Episode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val repository = SeriesRepository()

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()
    private val _showDetails = MutableStateFlow<com.example.pekseries.data.remote.TvMazeShowDto?>(null)
    val showDetails: StateFlow<com.example.pekseries.data.remote.TvMazeShowDto?> = _showDetails.asStateFlow()

    fun loadEpisodes(showId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                _showDetails.value = com.example.pekseries.data.NetworkClient.api.getShowById(showId)
            } catch (e: Exception) {
                _showDetails.value = null
            }

            val result = repository.getShowEpisodes(showId)
            _isSubscribed.value = repository.isSubscribed(showId)

            _episodes.value = result
            _isLoading.value = false
        }
    }

    fun toggleSubscription(showId: String) {
        viewModelScope.launch {
            val newState = repository.toggleSubscription(showId)
            _isSubscribed.value = newState
        }
    }
}