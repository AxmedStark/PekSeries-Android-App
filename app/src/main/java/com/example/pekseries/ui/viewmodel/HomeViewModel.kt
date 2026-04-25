package com.example.pekseries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.data.repository.SeriesRepository
import com.example.pekseries.model.Show
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val shows: List<Show>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {
    private val repository = SeriesRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val genreMap = mapOf(
        "Боевик / Приключения" to "10759",
        "Анимация / Аниме" to "16",
        "Комедия" to "35",
        "Криминал" to "80",
        "Документальный" to "99",
        "Драма" to "18",
        "Семейный" to "10751",
        "Фантастика" to "10765",
        "Детектив" to "9648",
        "Реалити" to "10764",
        "Ток-шоу" to "10767",
        "Вестерн" to "10770"
    )

    private val typeMap = mapOf(
        "Игровой сериал" to "4",
        "Мини-сериал" to "2",
        "Документальный" to "0",
        "Реалити-шоу" to "3",
        "Ток-шоу" to "5",
        "Новости" to "1"
    )

    init {
        loadEpisodes("Actual")
    }

    fun loadEpisodes(filterCategory: String = "Actual") {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val shows = when (filterCategory) {
                    "Популярное", "Popular" -> repository.getPopularToday()
                    "Будущие", "Upcoming" -> repository.getUpcomingPremieres()
                    "Актуальное", "Actual" -> repository.getTodayEpisodes()
                    else -> repository.getTodayEpisodes()
                }
                _uiState.value = HomeUiState.Success(shows)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    fun applyFilters(genre: String, type: String, year: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val genreId = if (genre == "Все" || genre == "Жанр") null else genreMap[genre]
                val typeId = if (type == "Все" || type == "Тип") null else typeMap[type]
                val yearQuery = if (year == "Все" || year == "Год") null else year

                val shows = repository.discoverShows(genreId, yearQuery, typeId)
                _uiState.value = HomeUiState.Success(shows)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Ошибка фильтрации: ${e.message}")
            }
        }
    }
}