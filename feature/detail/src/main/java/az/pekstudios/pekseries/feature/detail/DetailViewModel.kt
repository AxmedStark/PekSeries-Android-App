package az.pekstudios.pekseries.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.getOrDefault
import az.pekstudios.pekseries.core.domain.getOrNull
import az.pekstudios.pekseries.core.domain.repository.ShowRepository
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.model.Episode
import az.pekstudios.pekseries.core.model.ShowDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val details: ShowDetails? = null,
    val episodes: List<Episode> = emptyList(),
    val isLoading: Boolean = true,
    val isSubscribed: Boolean = false,
    val canSubscribe: Boolean = false,
    val error: DataError? = null,
)

private const val TVMAZE_ID_PREFIX = "tvmaze_"

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var currentTvMazeId: String? = null

    fun loadEpisodes(passedId: String) {
        viewModelScope.launch {
            currentTvMazeId = null
            _uiState.value = DetailUiState(isLoading = true)

            // Screens can be opened with either id: a push carries the TVMaze id,
            // the home grid carries the TMDB one.
            val fromTvMaze = passedId.startsWith(TVMAZE_ID_PREFIX)
            var mazeId = if (fromTvMaze) passedId.removePrefix(TVMAZE_ID_PREFIX) else null

            val tmdbId = if (fromTvMaze) {
                showRepository.findTmdbId(mazeId!!).getOrNull()
            } else {
                passedId
            }

            if (tmdbId == null) {
                _uiState.update { it.copy(isLoading = false, error = DataError.NotFound) }
                return@launch
            }

            when (val details = showRepository.getShowDetails(tmdbId)) {
                is PekResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = details.error) }
                    return@launch
                }

                is PekResult.Success -> {
                    _uiState.update { it.copy(details = details.data) }
                    if (mazeId == null) {
                        mazeId = showRepository.findTvMazeId(details.data).getOrNull()
                    }
                }
            }

            currentTvMazeId = mazeId

            if (mazeId == null) {
                // Details are still worth showing; only subscribing is unavailable.
                _uiState.update { it.copy(isLoading = false, canSubscribe = false) }
                return@launch
            }

            val episodes = showRepository.getEpisodes(mazeId)
            val subscribed = subscriptionRepository.isSubscribed(mazeId).getOrDefault(false)

            _uiState.update { current ->
                current.copy(
                    episodes = episodes.getOrDefault(emptyList()),
                    isSubscribed = subscribed,
                    canSubscribe = true,
                    isLoading = false,
                    error = (episodes as? PekResult.Failure)?.error,
                )
            }
        }
    }

    fun toggleSubscription() {
        val mazeId = currentTvMazeId ?: return
        viewModelScope.launch {
            when (val result = subscriptionRepository.toggleSubscription(mazeId)) {
                is PekResult.Success -> _uiState.update { it.copy(isSubscribed = result.data) }
                is PekResult.Failure -> _uiState.update { it.copy(error = result.error) }
            }
        }
    }
}
