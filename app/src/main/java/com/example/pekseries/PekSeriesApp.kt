package com.example.pekseries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekseries.ui.screen.*
import com.example.pekseries.ui.theme.*
import com.example.pekseries.ui.viewmodel.AuthViewModel

// 1. РОУТЕР: Эта функция решает, какой экран показать первым
@Composable
fun PekSeriesApp() {
    val authViewModel: AuthViewModel = viewModel()
    // Слушаем состояние авторизации
    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    if (isLoggedIn) {
        // Если вошли — показываем основное приложение
        PekSeriesMainContent(onLogout = { authViewModel.logout() })
    } else {
        // Если нет — показываем экран входа
        LoginScreen(authViewModel = authViewModel)
    }
}

// 2. ОСНОВНОЙ КОНТЕНТ: Это само приложение с нижней панелью
@Composable
fun PekSeriesMainContent(onLogout: () -> Unit) {
    var selectedScreen by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = PekDarkBg) {
                val items = listOf(
                    "Home" to Icons.Filled.Home,
                    "Search" to Icons.Filled.Search,
                    "Watchlist" to Icons.Filled.CalendarMonth,
                    "Profile" to Icons.Filled.Person
                )
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) },
                        selected = selectedScreen == index,
                        onClick = { selectedScreen = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PekRed,
                            selectedTextColor = PekRed,
                            indicatorColor = PekDarkBg,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(PekDarkBg)
            .padding(padding)) {
            when (selectedScreen) {
                0 -> HomeScreen()
                1 -> SearchScreen()
                2 -> UpcomingScreen()
                // Передаем функцию выхода в экран профиля
                3 -> ProfileScreen(onLogout)
            }
        }
    }
}