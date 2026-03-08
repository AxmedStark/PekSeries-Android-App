package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekseries.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: com.example.pekseries.ui.viewmodel.WatchlistViewModel = viewModel()
) {
    val stats by viewModel.profileStats.collectAsState()

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
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.DarkGray))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Axmed Stark", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("@AxmedStark • Baku, Azerbaijan", color = Primary, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard(stats.first.toString(), "SERIES")
            StatCard(stats.second.toString(), "EPISODES")
            StatCard(stats.third.toString(), "HOURS")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text("Preferences", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(10.dp))

        PreferenceItem("Push Notifications", true)
//        PreferenceItem("Telegram Sync", true)
//        PreferenceItem("Dark mode", true)

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
fun PreferenceItem(text: String, enabled: Boolean) {
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
                Icon(Icons.Filled.Settings, null, tint = Primary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = Color.White)
        }
        Switch(
            checked = enabled,
            onCheckedChange = {},
            colors = SwitchDefaults.colors(
                checkedThumbColor = Primary,
                checkedTrackColor = PekYellow
            )
        )
    }
}
