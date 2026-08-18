package az.pekstudios.pekseries

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import az.pekstudios.pekseries.core.ui.theme.*
import az.pekstudios.pekseries.feature.auth.LoginScreen
import az.pekstudios.pekseries.feature.auth.LoginViewModel
import az.pekstudios.pekseries.feature.detail.DetailScreen
import az.pekstudios.pekseries.feature.profile.ProfileScreen
import az.pekstudios.pekseries.feature.search.SearchScreen
import az.pekstudios.pekseries.feature.watchlist.WatchListScreen
import az.pekstudios.pekseries.feature.notifications.NotificationsScreen
import az.pekstudios.pekseries.feature.home.HomeScreen

@Composable
fun PekSeriesApp(
    pendingShowId: StateFlow<String?>,
    onShowIdHandled: () -> Unit,
) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val isLoggedIn by loginViewModel.isUserLoggedIn.collectAsState()
    val showId by pendingShowId.collectAsState()

    if (isLoggedIn) {
        val navController = rememberNavController()

        // Held rather than dropped while signed out: a push tapped on the login
        // screen still lands on the right show once the user gets in.
        LaunchedEffect(showId) {
            showId?.takeIf { it.isNotBlank() }?.let { id ->
                navController.navigate("detail/$id") {
                    launchSingleTop = true
                }
                onShowIdHandled()
            }
        }

        NavHost(navController = navController, startDestination = "main") {

            composable("main") {
                val context = LocalContext.current
                PekSeriesMainContent(
                    onLogout = { loginViewModel.logout(context) },
                    onNavigateToDetail = { showId -> navController.navigate("detail/$showId") },
                    onNavigateToNotifications = { navController.navigate("notifications") }
                )
            }

            composable("notifications") {
                NotificationsScreen(
                    onBack = { navController.popBackStack() }
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
        LoginScreen(loginViewModel = loginViewModel)
    }
}

/** Index of the Profile entry in the bottom navigation bar. */
private const val PROFILE_TAB_INDEX = 3

@Composable
fun PekSeriesMainContent(
    onLogout: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    var selectedScreen by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = selectedScreen != 0) {
        selectedScreen = 0
    }

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
                0 -> HomeScreen(
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToNotifications = onNavigateToNotifications,
                    // The avatar already had a click handler, but nothing was
                    // passed in, so it silently did nothing.
                    onNavigateToProfile = { selectedScreen = PROFILE_TAB_INDEX }
                )
                1 -> SearchScreen(
                    onNavigateToDetail = onNavigateToDetail
                )
                2 -> WatchListScreen(
                    onNavigateToDetail = onNavigateToDetail
                )
                3 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}