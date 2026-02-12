package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Импорты твоих файлов
import com.example.pekseries.model.Show
import com.example.pekseries.ui.theme.*

// ... твои импорты ...
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.pekseries.ui.viewmodels.HomeViewModel
import com.example.pekseries.ui.viewmodels.HomeUiState

@Composable
fun HomeScreen(
    // Android сам создаст ViewModel и переживет повороты экрана
    viewModel: HomeViewModel = viewModel()
) {
    // Подписываемся на состояние (как только ViewModel обновит данные, экран перерисуется)
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            // Header (оставляем как был)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
//                        Text("Welcome back", color = TextSecondary, fontSize = 12.sp)
                        Text("Hello, pek", color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Icon(Icons.Filled.Notifications, "Notify", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Just Released", color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                // Кнопка обновления (на случай ошибки)
                IconButton(onClick = { viewModel.loadEpisodes() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- ГЛАВНАЯ МАГИЯ ---
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }
            is HomeUiState.Error -> {
                item {
                    Text(text = state.message, color = Primary)
                }
            }
            is HomeUiState.Success -> {
                if (state.shows.isEmpty()) {
                    item { Text("Сегодня ничего не вышло 😔", color = Color.Gray) }
                } else {
                    items(state.shows) { show ->
                        HomeShowCard(
                            show = show,
                            onCheckClick = { viewModel.toggleWatched(show) } // Передаем клик во ViewModel
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HomeShowCard(
    show: Show,
    onCheckClick: () -> Unit // <-- Добавили колбэк
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(60.dp).height(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (show.isNew) {
                Text("AIRED TODAY", color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(show.title, color = PekYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(show.episode, color = TextPrimary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(show.time, color = TextSecondary, fontSize = 12.sp)
            }
        }
        IconButton(onClick = onCheckClick) { // <-- Вызываем колбэк при клике
            Icon(
                imageVector = if (show.isWatched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle, // Меняем иконку (закрашенная/пустая)
                contentDescription = null,
                tint = if (show.isWatched) PekYellow else TextSecondary // Меняем цвет (Красный/Серый)
            )
        }
    }
}