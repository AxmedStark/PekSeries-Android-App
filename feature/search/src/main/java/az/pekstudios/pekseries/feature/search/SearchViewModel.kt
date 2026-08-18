package az.pekstudios.pekseries.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.ShowRepository
import az.pekstudios.pekseries.core.model.Show
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SearchUiState {
    /** Query too short to be worth a request. */
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val shows: List<Show>) : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data class Error(val error: DataError) : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val showRepository: ShowRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * debounce + flatMapLatest replaces the hand-rolled `searchJob?.cancel()`
     * plus `delay(500)`. flatMapLatest cancels the in-flight request when a new
     * query arrives, so a slow response for "brea" can no longer land after
     * "breaking bad" and overwrite the newer results.
     */
    val uiState: StateFlow<SearchUiState> = _searchQuery
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            flow {
                if (query.trim().length < MIN_QUERY_LENGTH) {
                    emit(SearchUiState.Idle)
                    return@flow
                }

                emit(SearchUiState.Loading)
                emit(
                    when (val result = showRepository.search(query.trim())) {
                        is PekResult.Failure -> SearchUiState.Error(result.error)
                        is PekResult.Success ->
                            if (result.data.isEmpty()) SearchUiState.Empty(query)
                            else SearchUiState.Success(result.data)
                    },
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SearchUiState.Idle,
        )

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    private companion object {
        const val DEBOUNCE_MS = 400L
        const val MIN_QUERY_LENGTH = 3
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
