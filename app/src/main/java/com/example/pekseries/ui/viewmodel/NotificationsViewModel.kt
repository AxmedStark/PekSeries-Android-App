package com.example.pekseries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekseries.data.repository.SeriesRepository.PekNotification
import com.example.pekseries.data.repository.SeriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val repository = SeriesRepository()
    private val _notifications = MutableStateFlow<List<PekNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.value = repository.getNotifications()
        }
    }
}