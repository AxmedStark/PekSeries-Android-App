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

@Composable
fun HomeScreen() {
    val newShows = listOf(
        Show("The Bear", "S03 • Episode 01", "34 min", true),
        Show("House of the Dragon", "S02 • Episode 04", "58 min", true),
        Show("The Boys", "S04 • Episode 05", "62 min", true)
    )

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Welcome back", color = TextSecondary, fontSize = 12.sp)
                        Text("Hello, Alex", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                Text("Just Released", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("See All", color = Red, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(newShows) { show ->
            HomeShowCard(show)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun HomeShowCard(show: Show) {
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
                Text("AIRED TODAY", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(show.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(show.episode, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(show.time, color = TextSecondary, fontSize = 12.sp)
            }
        }
        IconButton(onClick = { }) {
            Icon(Icons.Outlined.CheckCircle, null, tint = TextSecondary)
        }
    }
}