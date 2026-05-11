package az.pekstudios.pekseries.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.network.repository.SeriesRepository
import az.pekstudios.pekseries.core.network.repository.SeriesRepository.PekNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: SeriesRepository
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<PekNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.value = repository.getNotifications()
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
            _notifications.value = emptyList()
        }
    }
}