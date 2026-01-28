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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pekseries.ui.theme.*

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            Text("Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.Settings, null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(30.dp))

        Box {
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray))
            Box(modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Red, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("PRO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Alex Johnson", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Pro Member • Since 2022", color = Red, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard("124", "SERIES")
            StatCard("3,520", "EPISODES")
            StatCard("1,840", "HOURS")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text("Preferences", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(10.dp))

        PreferenceItem("Push Notifications", true)
        PreferenceItem("Telegram Sync", true)
        PreferenceItem("Dark Mode", true)

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = {},
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E1212)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
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
            Box(modifier = Modifier.size(32.dp).background(Color(0xFF2B1212), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Settings, null, tint = Red, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = Color.White)
        }
        Switch(checked = enabled, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color.Gray))
    }
}