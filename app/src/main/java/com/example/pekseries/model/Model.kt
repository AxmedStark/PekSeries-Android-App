package com.example.pekseries.model

import androidx.compose.ui.graphics.Color

data class Show(
    val title: String,
    val episode: String,
    val time: String,
    val isNew: Boolean = false
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