package az.pekstudios.pekseries.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.usecase.GetWatchStatsUseCase
import az.pekstudios.pekseries.core.model.WatchStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val stats: WatchStats = WatchStats.EMPTY,
    val isLoadingStats: Boolean = true,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getWatchStats: GetWatchStatsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoadingStats = true)

            // Stats are a secondary detail on this screen, so a failure shows
            // zeroes rather than taking over the whole profile with an error.
            val stats = when (val result = getWatchStats()) {
                is PekResult.Success -> result.data
                is PekResult.Failure -> WatchStats.EMPTY
            }
            _uiState.value = ProfileUiState(stats = stats, isLoadingStats = false)
        }
    }
}
