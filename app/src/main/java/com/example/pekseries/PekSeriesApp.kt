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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun PekSeriesApp() {
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    if (isLoggedIn) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "main") {

            composable("main") {
                PekSeriesMainContent(
                    onLogout = { authViewModel.logout() },
                    onNavigateToDetail = { showId ->
                        navController.navigate("detail/$showId")
                    }
                )
            }

            composable("detail/{showId}") { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                DetailScreen(
                    showId = showId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    } else {
        LoginScreen(authViewModel = authViewModel)
    }
}
//@Composable
//fun PekSeriesApp() {
//    val authViewModel: AuthViewModel = viewModel()
//    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
//
//    if (isLoggedIn) {
//        PekSeriesMainContent(onLogout = { authViewModel.logout() })
//    } else {
//        LoginScreen(authViewModel = authViewModel)
//    }
//}

@Composable
fun PekSeriesMainContent(
    onLogout: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
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
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
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
                0 -> HomeScreen(onNavigateToDetail = onNavigateToDetail)
                1 -> SearchScreen(onNavigateToDetail = onNavigateToDetail)
                2 -> UpcomingScreen()
                3 -> ProfileScreen(onLogout)
            }
        }
    }
}
