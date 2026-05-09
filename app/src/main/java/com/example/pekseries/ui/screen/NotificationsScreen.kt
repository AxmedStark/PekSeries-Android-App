package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pekseries.ui.theme.*
import com.example.pekseries.ui.viewmodel.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearNotifications() }) {
                            Icon(Icons.Filled.Delete, null, tint = Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications yet", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                items(notifications) { notify ->
                    NotificationItem(notify)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notify: com.example.pekseries.data.repository.SeriesRepository.PekNotification) {
    val date = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(notify.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, null, tint = Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(notify.title, color = PekYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(notify.message, color = Color.White, fontSize = 13.sp)
            Text(date, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}