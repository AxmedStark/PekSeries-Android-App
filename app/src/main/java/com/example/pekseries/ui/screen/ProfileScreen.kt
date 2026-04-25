package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekseries.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.ui.platform.LocalContext
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.pekseries.service.EpisodeCheckWorker
import com.example.pekseries.worker.NewEpisodeWorker

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: com.example.pekseries.ui.viewmodel.WatchlistViewModel = viewModel()
) {
    val context = LocalContext.current
    val stats by viewModel.profileStats.collectAsState()

    // Инициализируем SharedPreferences
    val prefs = remember { context.getSharedPreferences("pek_prefs", android.content.Context.MODE_PRIVATE) }

    // Читаем сохраненное состояние (по умолчанию false)
    var pushEnabled by remember { mutableStateOf(prefs.getBoolean("push_enabled", false)) }

    val onTogglePush = { isChecked: Boolean ->
        pushEnabled = isChecked
        // Сохраняем выбор пользователя
        prefs.edit().putBoolean("push_enabled", isChecked).apply()

        if (isChecked) {
            // Запускаем воркер на 30 минут
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NewEpisodeWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DailyEpisodeCheck",
                ExistingPeriodicWorkPolicy.UPDATE, // UPDATE перепишет старую задачу на 30-минутную
                workRequest
            )
        } else {
            // Если выключил — полностью отменяем задачу
            WorkManager.getInstance(context).cancelUniqueWork("DailyEpisodeCheck")
        }
    }

    // В UI части передай эти данные в свой PreferenceItem:
    PreferenceItem(
        text = "Push Notifications",
        icon = Icons.Filled.Notifications,
        checked = pushEnabled,
        onCheckedChange = { onTogglePush(it) }
    )

    // Достаем реальные данные пользователя из Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "user@pekseries.com"

    LaunchedEffect(Unit) {
        viewModel.loadProfileStats()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Transparent) // Заглушка для центровки
            Text("Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.Settings, null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(30.dp))

        Box {
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = "Avatar", tint = Color.Gray, modifier = Modifier.size(50.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Axmed Stark", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(userEmail, color = Primary, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(4.dp))
        Text("Baku, Azerbaijan", color = Color.Gray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard(stats.first.toString(), "SERIES")
            StatCard(stats.second.toString(), "EPISODES")
            StatCard(stats.third.toString(), "HOURS")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text("Preferences", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(10.dp))

        PreferenceItem(
            text = "Push Notifications",
            icon = Icons.Filled.Notifications,
            checked = pushEnabled,
            onCheckedChange = { isChecked -> onTogglePush(isChecked) }
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onLogout,
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PekYellow),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out")
        }
    }
}

@Composable
fun StatCard(number: String, label: String) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}

@Composable
fun PreferenceItem(text: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(Primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Primary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = Color.White)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Primary,
                checkedTrackColor = Primary.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

// Функция для настройки фоновой работы
private fun scheduleDailyCheck(context: android.content.Context) {
    // Условие: задача запустится только если есть интернет
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workRequest = PeriodicWorkRequestBuilder<EpisodeCheckWorker>(30, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

    // Добавляем задачу в систему (уникальное имя не даст запустить ее дважды)
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "DailyEpisodeCheck",
        ExistingPeriodicWorkPolicy.UPDATE,
        workRequest
    )
}