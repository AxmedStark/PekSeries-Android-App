package com.example.pekseries.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.data.repository.SeriesRepository
import com.example.pekseries.model.Show
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Состояния экрана (что мы показываем пользователю)
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val shows: List<Show>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {
    // Создаем репозиторий (в больших проектах это делается через Hilt/Koin, но пока так)
    private val repository = SeriesRepository()

    // Хранилище состояния (по умолчанию - Загрузка)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Как только экран создался — грузим данные
        loadEpisodes()
    }

    fun loadEpisodes() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Идем в репозиторий за данными
                val shows = repository.getTodayEpisodes()
                _uiState.value = HomeUiState.Success(shows)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    fun toggleWatched(show: Show) {
        viewModelScope.launch {
            // 1. Сразу обновляем UI (оптимистичное обновление), чтобы галочка нажалась мгновенно
            val currentState = _uiState.value
            if (currentState is HomeUiState.Success) {
                val updatedList = currentState.shows.map {
                    if (it.id == show.id) it.copy(isWatched = !it.isWatched) else it
                }
                _uiState.value = HomeUiState.Success(updatedList)
            }

            // 2. Отправляем данные на сервер (Firebase)
            // Если галочку поставили (было false -> стало true)
            if (!show.isWatched) {
                repository.markEpisodeAsWatched(show.id)
            }
            // (Логику снятия галочки можно добавить в репозиторий позже)
        }
    }
}