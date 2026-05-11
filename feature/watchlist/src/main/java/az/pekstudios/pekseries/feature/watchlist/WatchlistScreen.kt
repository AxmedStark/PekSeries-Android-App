package az.pekstudios.pekseries.feature.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.pekstudios.pekseries.core.ui.theme.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import az.pekstudios.pekseries.core.model.Show

@Composable
fun WatchListScreen(
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Subscriptions")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Watchlist", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = Primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Primary
                )
            },
            divider = { HorizontalDivider(color = Color.DarkGray) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) },
                    selectedContentColor = Primary,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 -> UpcomingContent(viewModel, onNavigateToDetail)
            1 -> SubscriptionsContent(
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToDetail
            )
        }
    }
}

@Composable
fun UpcomingContent(
    viewModel: WatchlistViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val todayEpisodes by viewModel.todayEpisodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTodayEpisodes()
    }

    if (isLoading && todayEpisodes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    } else if (todayEpisodes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No upcoming episodes for your subscriptions", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Upcoming Releases",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(todayEpisodes) { show ->
                UpcomingEpisodeCard(show = show, onClick = { onNavigateToDetail(show.id) })
            }
        }
    }
}

@Composable
fun SubscriptionsContent(
    viewModel: WatchlistViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSubscriptions()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    } else if (subscriptions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No subscriptions found", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            items(subscriptions) { show ->
                SubscriptionCard(show = show, onClick = { onNavigateToDetail(show.id) })
            }
        }
    }
}

@Composable
fun UpcomingEpisodeCard(show: Show, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = show.dateDisplay ?: "",
                color = PekYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = show.time ?: "",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(
                text = show.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = show.episode ?: "",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        AsyncImage(
            model = show.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(width = 55.dp, height = 80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun SubscriptionCard(show: Show, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = show.getPosterUrl(),
            contentDescription = null,
            modifier = Modifier.size(60.dp, 80.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(show.title, color = PekYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(show.episode ?: "", color = TextPrimary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(show.time ?: "", color = TextSecondary, fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF03DAC5),
            modifier = Modifier.size(24.dp).padding(end = 4.dp)
        )
    }
}