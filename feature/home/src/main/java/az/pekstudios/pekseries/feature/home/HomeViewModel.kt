package az.pekstudios.pekseries.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.ShowRepository
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

    /** Reached the server, but there is genuinely nothing to show. */
    data object Empty : HomeUiState

    data class Error(val error: DataError) : HomeUiState
}

enum class HomeCategory(val label: String) {
    AiringToday("Airing Today"),
    Popular("Popular"),
    Upcoming("Upcoming"),
    ;

    companion object {
        fun fromLabel(label: String): HomeCategory =
            entries.firstOrNull { it.label == label } ?: AiringToday
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val showRepository: ShowRepository,
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
        "Western" to "10770",
    )

    private val typeMap = mapOf(
        "Scripted" to "4",
        "Miniseries" to "2",
        "Documentary" to "0",
        "Reality" to "3",
        "Talk Show" to "5",
        "News" to "1",
    )

    init {
        loadEpisodes()
    }

    fun loadEpisodes(filterCategory: String = HomeCategory.AiringToday.label) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            val result = when (HomeCategory.fromLabel(filterCategory)) {
                HomeCategory.Popular -> showRepository.getTrending()
                HomeCategory.Upcoming -> showRepository.getUpcomingPremieres()
                HomeCategory.AiringToday -> showRepository.getAiringToday()
            }
            _uiState.value = result.toUiState()
        }
    }

    fun applyFilters(genre: String, type: String, year: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            _uiState.value = showRepository.discover(
                genreId = genreMap[genre]?.takeUnless { genre.isUnset() },
                year = year.takeUnless { it.isUnset() },
                typeId = typeMap[type]?.takeUnless { type.isUnset() },
            ).toUiState()
        }
    }

    fun retry() = loadEpisodes()

    private fun String.isUnset(): Boolean = this == "All" || this in UNSET_LABELS

    private fun PekResult<List<Show>>.toUiState(): HomeUiState = when (this) {
        is PekResult.Failure -> HomeUiState.Error(error)
        is PekResult.Success -> if (data.isEmpty()) HomeUiState.Empty else HomeUiState.Success(data)
    }

    private companion object {
        val UNSET_LABELS = setOf("Genre", "Type", "Year")
    }
}
