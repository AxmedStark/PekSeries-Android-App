package az.pekstudios.pekseries.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.network.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: SeriesRepository
) : ViewModel() {

    private val _profileStats = MutableStateFlow(Triple(0, 0, 0))
    val profileStats: StateFlow<Triple<Int, Int, Int>> = _profileStats.asStateFlow()

    init {
        loadProfileStats()
    }

    private fun loadProfileStats() {
        viewModelScope.launch {
            val subs = repository.getSubscribedShows()
            if (subs.isEmpty()) {
                _profileStats.value = Triple(0, 0, 0)
                return@launch
            }

            val results = coroutineScope {
                subs.map { show ->
                    async {
                        try {
                            val cleanId = show.id.removePrefix("tvmaze_")
                            val episodes = repository.getShowEpisodes(cleanId)

                            val totalMinutes = episodes.sumOf { it.runtime ?: 45 }
                            Pair(episodes.size, totalMinutes)
                        } catch (e: Exception) {
                            Pair(0, 0)
                        }
                    }
                }.awaitAll()
            }

            val totalEpisodes = results.sumOf { it.first }
            val totalHours = results.sumOf { it.second } / 60

            _profileStats.value = Triple(subs.size, totalEpisodes, totalHours)
        }
    }
}