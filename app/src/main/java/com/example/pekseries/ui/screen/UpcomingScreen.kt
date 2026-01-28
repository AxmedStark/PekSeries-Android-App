package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    val todayShows = listOf(
        TimelineItem("08:00", "The Boys", "S04 • E06 'Dirty Business'", true),
        TimelineItem("09:00", "House of Dragon", "S02 • E03 'The Burning Mill'", true),
        TimelineItem("10:30", "The Bear", "S03 • E01 'Tomorrow'", true)
    )
    val tomorrowShows = listOf(
        TimelineItem("07:00", "Presumed Innocent", "S01 • E04"),
        TimelineItem("08:00", "The Acolyte", "S01 • E05")
    )

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Upcoming", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.Person, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Watchlist", "All Shows", "Premieres").forEach {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(it, color = if(it=="Watchlist") Red else Color.Gray, fontWeight = FontWeight.Bold)
                        if(it=="Watchlist") Box(modifier = Modifier.width(20.dp).height(2.dp).background(Red))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Today", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("LIVE UPDATES", color = Red, fontSize = 10.sp, modifier = Modifier.background(Red.copy(alpha=0.2f), RoundedCornerShape(4.dp)).padding(4.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(todayShows) { show ->
            TimelineRow(show)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Tomorrow, June 28", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(tomorrowShows) { show ->
            TimelineRow(show)
        }
    }
}

@Composable
fun TimelineRow(item: TimelineItem) {
    IntrinsicSize.Min
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(50.dp)) {
            Text(item.time, color = if(item.isLive) Red else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if(item.isLive) Red else Color.Gray))
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