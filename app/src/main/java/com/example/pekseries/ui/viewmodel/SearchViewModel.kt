package com.example.pekseries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.model.SearchResponseItem // Твоя модель
import com.example.pekseries.data.repository.SeriesRepository // Твой репозиторий
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private var searchJob: Job? = null
    private val repository = SeriesRepository() // Подключаем репозиторий

    // Текст, который пользователь вводит в строку поиска
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Список найденных сериалов
    private val _searchResults = MutableStateFlow<List<SearchResponseItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResponseItem>> = _searchResults

    // Состояние загрузки (крутилка)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Обновляем текст и сразу ищем
    fun onQueryChange(query: String) {
        _searchQuery.value = query

        // 1. Если пользователь продолжает печатать - отменяем прошлый незаконченный поиск
        searchJob?.cancel()

        // 2. Запускаем новый таймер
        searchJob = viewModelScope.launch {
            delay(500) // Ждем 500 миллисекунд (полсекунды) после последнего нажатия

            if (query.length > 2) {
                _isLoading.value = true
                try {
                    // Идем в интернет только сейчас
                    val results = repository.searchSeries(query)
                    _searchResults.value = results
                } catch (e: Exception) {
                    _searchResults.value = emptyList()
                } finally {
                    _isLoading.value = false
                }
            } else {
                _searchResults.value = emptyList() // Очищаем список, если текст короткий
            }
        }
    }

//    private fun performSearch(query: String) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                // Идем в интернет за данными
//                val results = repository.searchSeries(query)
//                _searchResults.value = results
//            } catch (e: Exception) {
//                // Если ошибка (например, нет интернета) - просто показываем пустой список
//                _searchResults.value = emptyList()
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
}