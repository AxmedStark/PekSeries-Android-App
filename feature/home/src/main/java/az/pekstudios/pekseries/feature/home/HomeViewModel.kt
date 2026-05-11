package az.pekstudios.pekseries.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.network.repository.SeriesRepository
import az.pekstudios.pekseries.core.model.Show
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val shows: List<Show>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SeriesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val genreMap = mapOf(
        "Action & Adventure" to "10759",
        "Animation" to "16",
        "Comedy" to "35",
        "Crime" to "80",
        "Documentary" to "99",
        "Drama" to "18",
        "Family" to "10751",
        "Sci-Fi & Fantasy" to "10765",
        "Mystery" to "9648",
        "Reality" to "10764",
        "Talk" to "10767",
        "Western" to "10770"
    )

    private val typeMap = mapOf(
        "Scripted" to "4",
        "Miniseries" to "2",
        "Documentary" to "0",
        "Reality" to "3",
        "Talk Show" to "5",
        "News" to "1"
    )

    init {
        loadEpisodes("Airing Today")
    }

    fun loadEpisodes(filterCategory: String = "Airing Today") {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val shows = when (filterCategory) {
                    "Popular" -> repository.getPopularToday()
                    "Upcoming" -> repository.getUpcomingPremieres()
                    "Airing Today" -> repository.getTodayEpisodes()
                    else -> repository.getTodayEpisodes()
                }
                _uiState.value = HomeUiState.Success(shows)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun applyFilters(genre: String, type: String, year: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val genreId = if (genre == "All" || genre == "Genre") null else genreMap[genre]
                val typeId = if (type == "All" || type == "Type") null else typeMap[type]
                val yearQuery = if (year == "All" || year == "Year") null else year

                val shows = repository.discoverShows(genreId, yearQuery, typeId)
                _uiState.value = HomeUiState.Success(shows)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Filter error: ${e.message}")
            }
        }
    }
}