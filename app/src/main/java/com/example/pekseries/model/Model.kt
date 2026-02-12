package com.example.pekseries.model

import androidx.compose.ui.graphics.Color

data class Show(
    val id: String, // ID от TVMaze (нужен для базы)
    val title: String,
    val episode: String,
    val time: String,
    val imageUrl: String,
    val isNew: Boolean = false,
    val isWatched: Boolean = false // <-- Новое поле
)

data class TrendingItem(
    val title: String,
    val color: Color
)

data class TimelineItem(
    val time: String,
    val title: String,
    val sub: String,
    val isLive: Boolean = false
)