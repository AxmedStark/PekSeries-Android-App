package com.example.pekseries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.model.SearchResponseItem
import com.example.pekseries.data.repository.SeriesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private var searchJob: Job? = null
    private val repository = SeriesRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<SearchResponseItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResponseItem>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun onQueryChange(query: String) {
        _searchQuery.value = query

        searchJob?.cancel()

        // 2. Запускаем новый таймер
        searchJob = viewModelScope.launch {
            delay(500)

            if (query.length > 2) {
                _isLoading.value = true
                try {
                    val results = repository.searchSeries(query)
                    _searchResults.value = results
                } catch (e: Exception) {
                    _searchResults.value = emptyList()
                } finally {
                    _isLoading.value = false
                }
            } else {
                _searchResults.value = emptyList()
            }
        }
    }
}