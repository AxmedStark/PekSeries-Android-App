package az.pekstudios.pekseries.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.network.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun loadProfileStats() {
        viewModelScope.launch {
            try {
                val subscribedShows = repository.getSubscribedShows()
                val seriesCount = subscribedShows.size

                // Здесь можно добавить логику подсчета эпизодов и часов,
                // если она есть в репозитории. Пока ставим заглушку или твои расчеты.
                val episodesCount = seriesCount * 10 // Пример
                val hoursCount = (episodesCount * 45) / 60 // Пример: 45 мин на серию

                _profileStats.value = Triple(seriesCount, episodesCount, hoursCount)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }
}