package com.example.pekseries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.data.remote.TmdbShowDetailDto
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

    // Новая переменная: можно ли вообще подписаться? (Есть ли сериал в TVMaze)
    private val _canSubscribe = MutableStateFlow(false)
    val canSubscribe: StateFlow<Boolean> = _canSubscribe.asStateFlow()

    private val _showDetails = MutableStateFlow<TmdbShowDetailDto?>(null)
    val showDetails: StateFlow<TmdbShowDetailDto?> = _showDetails.asStateFlow()

    private val _trailerKey = MutableStateFlow<String?>(null)
    val trailerKey: StateFlow<String?> = _trailerKey.asStateFlow()

    private var currentTvMazeId: String? = null

    fun loadEpisodes(passedId: String) {
        viewModelScope.launch {
            // Очищаем старые данные
            _showDetails.value = null
            _trailerKey.value = null
            _episodes.value = emptyList()
            _isSubscribed.value = false
            _canSubscribe.value = false
            currentTvMazeId = null

            _isLoading.value = true

            var tmdbId: String? = passedId
            var mazeId: String? = null

            // 1. ПРОВЕРЯЕМ, ОТКУДА МЫ ПРИШЛИ
            if (passedId.startsWith("tvmaze_")) {
                // Пришли из Подписок! Включаем Обратный Мост
                mazeId = passedId.removePrefix("tvmaze_")
                tmdbId = repository.getTmdbIdByTvMazeId(mazeId)
            }

            // 2. ЕСЛИ ЕСТЬ TMDB ID, ГРУЗИМ КРАСОТУ И ТРЕЙЛЕРЫ
            if (tmdbId != null) {
                val details = repository.getShowDetails(tmdbId)
                _showDetails.value = details

                _trailerKey.value = details?.videos?.results?.firstOrNull {
                    it.site == "YouTube" && it.type == "Trailer"
                }?.key ?: details?.videos?.results?.firstOrNull { it.site == "YouTube" }?.key

                // Если пришли с Главной, mazeId еще пустой, включаем Прямой Мост
                if (mazeId == null) {
                    mazeId = repository.findTvMazeId(details)
                }
            }

            currentTvMazeId = mazeId

            // 3. ЕСЛИ ЕСТЬ TVMAZE ID, ГРУЗИМ СЕРИИ И КНОПКУ ПОДПИСКИ
            if (mazeId != null) {
                _canSubscribe.value = true
                _isSubscribed.value = repository.isSubscribed(mazeId)
                _episodes.value = repository.getShowEpisodes(mazeId)
            } else {
                _episodes.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    fun toggleSubscription() {
        viewModelScope.launch {
            currentTvMazeId?.let { mazeId ->
                val newState = repository.toggleSubscription(mazeId)
                _isSubscribed.value = newState
            }
        }
    }
}