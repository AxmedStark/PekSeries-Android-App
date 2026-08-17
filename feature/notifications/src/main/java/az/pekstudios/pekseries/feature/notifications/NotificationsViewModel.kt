package az.pekstudios.pekseries.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.feature.notifications.data.NotificationDao
import az.pekstudios.pekseries.feature.notifications.data.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationDao: NotificationDao // Внедряем DAO напрямую
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications = _notifications.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch {
            notificationDao.getAllNotifications().collect {
                _notifications.value = it
            }
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            notificationDao.deleteAllNotifications()
        }
    }
}