package az.pekstudios.pekseries.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.UserProfileRepository
import az.pekstudios.pekseries.core.domain.usecase.GetWatchStatsUseCase
import az.pekstudios.pekseries.core.model.UserProfile
import az.pekstudios.pekseries.core.model.WatchStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val stats: WatchStats = WatchStats.EMPTY,
    val isLoadingStats: Boolean = true,
    val isEditingProfile: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isTogglingPush: Boolean = false,
    val error: DataError? = null,
)

const val MAX_DISPLAY_NAME_LENGTH = 30

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val getWatchStats: GetWatchStatsUseCase,
) : ViewModel() {

    val profile: StateFlow<UserProfile> = userProfileRepository.profile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UserProfile(),
        )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true) }

            // Stats are a secondary detail here, so a failure shows zeroes
            // rather than taking over the whole screen with an error.
            val stats = (getWatchStats() as? PekResult.Success)?.data ?: WatchStats.EMPTY
            _uiState.update { it.copy(stats = stats, isLoadingStats = false) }
        }
    }

    fun startEditingProfile() = _uiState.update { it.copy(isEditingProfile = true) }

    fun cancelEditingProfile() = _uiState.update { it.copy(isEditingProfile = false) }

    /** Blank clears the override and restores the name from the auth provider. */
    fun saveDisplayName(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true) }

            val trimmed = name.trim().take(MAX_DISPLAY_NAME_LENGTH)
            val result = userProfileRepository.updateDisplayName(trimmed.ifBlank { null })

            _uiState.update {
                it.copy(
                    isSavingProfile = false,
                    isEditingProfile = false,
                    error = (result as? PekResult.Failure)?.error,
                )
            }
        }
    }

    fun savePhotoUri(uri: String?) {
        viewModelScope.launch {
            val result = userProfileRepository.updatePhotoUri(uri)
            _uiState.update { it.copy(error = (result as? PekResult.Failure)?.error) }
        }
    }

    /**
     * The switch is deliberately never disabled. The preference is written
     * locally first, so it flips immediately; topic reconciliation continues in
     * the background and only reports if it fails.
     */
    fun setPushEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingPush = true) }
            val result = userProfileRepository.setPushEnabled(enabled)
            _uiState.update {
                it.copy(
                    isTogglingPush = false,
                    error = (result as? PekResult.Failure)?.error,
                )
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
