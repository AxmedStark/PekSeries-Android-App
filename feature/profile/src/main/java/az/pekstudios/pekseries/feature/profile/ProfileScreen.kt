package az.pekstudios.pekseries.feature.profile

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import az.pekstudios.pekseries.core.ui.theme.*
import az.pekstudios.pekseries.core.work.PekAlarmManager
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel() // Теперь используем правильную VM
) {
    val context = LocalContext.current
    val stats by viewModel.profileStats.collectAsState()
    val prefs = remember { context.getSharedPreferences("pek_prefs", Context.MODE_PRIVATE) }

    var pushEnabled by remember { mutableStateOf(prefs.getBoolean("push_enabled", true)) }

    val onTogglePush = { isChecked: Boolean ->
        pushEnabled = isChecked
        prefs.edit().putBoolean("push_enabled", isChecked).apply()

        if (isChecked) {
            PekAlarmManager.scheduleNextAlarm(context)
        } else {
            PekAlarmManager.cancelAlarm(context)
        }
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName ?: "Киноман"
    val userEmail = currentUser?.email ?: ""
    val photoUrl = currentUser?.photoUrl

    // Статистика загружается автоматически при создании ViewModel (блок init)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Базовый темный фон
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            Text("Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.Settings, null, tint = Color.White)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(50.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(userName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(userEmail, color = Primary, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(30.dp))

        // Stats Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard(stats.first.toString(), "SERIES")
            StatCard(stats.second.toString(), "EPISODES")
            StatCard(stats.third.toString(), "HOURS")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            "Preferences",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        PreferenceItem(
            text = "Push Notifications",
            icon = Icons.Filled.Notifications,
            checked = pushEnabled,
            onCheckedChange = { isChecked -> onTogglePush(isChecked) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        OutlinedButton(
            onClick = onLogout,
            border = BorderStroke(1.dp, Primary.copy(alpha = 0.5f)),
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
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
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
                checkedTrackColor = Primary.copy(alpha = 0.5f)
            )
        )
    }
}