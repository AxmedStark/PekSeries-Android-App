package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.pekseries.model.TimelineItem
import com.example.pekseries.ui.theme.*

@Composable
fun UpcomingScreen() {
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
            0 -> UpcomingContent()
            1 -> SubscriptionsContent()
        }
    }
}

@Composable
fun UpcomingContent() {
    val todayShows = listOf(
        TimelineItem("08:00", "The Boys", "S04 • E06 'Dirty Business'", true),
        TimelineItem("09:00", "House of Dragon", "S02 • E03 'The Burning Mill'", true),
        TimelineItem("10:30", "The Bear", "S03 • E01 'Tomorrow'", true)
    )
    val tomorrowShows = listOf(
        TimelineItem("07:00", "Presumed Innocent", "S01 • E04"),
        TimelineItem("08:00", "The Acolyte", "S01 • E05")
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Today", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("LIVE UPDATES", color = DarkBg, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(PekYellow, RoundedCornerShape(4.dp)).padding(4.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(todayShows) { show -> TimelineRow(show) }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Tomorrow, June 28", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(tomorrowShows) { show -> TimelineRow(show) }
    }
}

@Composable
fun SubscriptionsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Здесь будут карточки сериалов,\nна которые ты подписался", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun TimelineRow(item: TimelineItem) {
    IntrinsicSize.Min
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(50.dp)) {
            Text(item.time, color = if(item.isLive) PekYellow else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if(item.isLive) PekYellow else Color.Gray))
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.DarkGray))
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(item.sub, color = Color.Gray, fontSize = 14.sp)
                }
                Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray))
            }
        }
    }
}
