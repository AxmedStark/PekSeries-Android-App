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

    // Модель из TMDB для красивого отображения постеров и описания
    private val _showDetails = MutableStateFlow<TmdbShowDetailDto?>(null)
    val showDetails: StateFlow<TmdbShowDetailDto?> = _showDetails.asStateFlow()

    // Скрытая переменная: храним TVMaze ID для подписок и серий
    private var currentTvMazeId: String? = null

    // showId здесь — это TMDB ID, который пришел с Главной страницы или Поиска
    fun loadEpisodes(showId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Сначала качаем красивые детали из TMDB (описание, постеры, IMDB ID)
            _showDetails.value = repository.getShowDetails(showId)

            // 2. Идем по мосту: просим Репозиторий найти TVMaze ID по этому TMDB ID
            currentTvMazeId = repository.getTvMazeId(showId)

            if (currentTvMazeId != null) {
                // 3. Ура, мост сработал! Проверяем подписку по TVMaze ID
                _isSubscribed.value = repository.isSubscribed(currentTvMazeId!!)

                // 4. Качаем список серий из TVMaze
                val result = repository.getShowEpisodes(currentTvMazeId!!)
                _episodes.value = result
            } else {
                // Если у сериала вдруг нет IMDB ID (очень редкий случай для старых шоу)
                _episodes.value = emptyList()
                _isSubscribed.value = false
            }

            _isLoading.value = false
        }
    }

    // Обрати внимание: мы больше не передаем сюда showId из UI!
    // ViewModel сама знает, какой TVMaze ID использовать.
    fun toggleSubscription() {
        viewModelScope.launch {
            currentTvMazeId?.let { mazeId ->
                val newState = repository.toggleSubscription(mazeId)
                _isSubscribed.value = newState
            }
        }
    }
}