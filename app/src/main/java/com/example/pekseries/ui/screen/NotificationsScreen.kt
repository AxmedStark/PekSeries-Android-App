package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pekseries.ui.theme.CardBg
import com.example.pekseries.ui.theme.DarkBg
import com.example.pekseries.ui.theme.PekYellow
import com.example.pekseries.ui.theme.Primary

@Composable
fun NotificationsScreen(onBackClick: () -> Unit) {
    val notifications = listOf(
        NotificationItem("Новая серия!", "S04E06 сериала 'The Boys' уже доступна.", "Только что"),
        NotificationItem("Премьера", "Вышел первый эпизод 'Daredevil: Born Again'.", "2 часа назад"),
        NotificationItem("PekSeries Pro", "Добро пожаловать! Ваша подписка активна.", "Вчера"),
        NotificationItem("Напоминание", "Вы не досмотрели 'House of the Dragon'.", "3 дня назад")
    )

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // Шапка
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Notifications", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(notifications) { notif ->
                NotificationCard(notif)
            }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = Primary)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.message, color = Color.LightGray, fontSize = 14.sp, lineHeight = 18.sp)
        }

        Text(item.time, color = PekYellow, fontSize = 12.sp)
    }
}

data class NotificationItem(val title: String, val message: String, val time: String)