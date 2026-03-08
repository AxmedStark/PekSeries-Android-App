package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pekseries.model.Episode
import com.example.pekseries.ui.component.VideoPlayer
import com.example.pekseries.ui.theme.DarkBg
import com.example.pekseries.ui.theme.Primary
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

@Composable
fun DetailScreen(
    showId: String,
    onBackClick: () -> Unit
) {
    var episodes by remember { mutableStateOf(listOf(
        Episode("3", "The Final Battle", season = 1, number = 3, airdate = "2024-03-10"),
        Episode("2", "The Betrayal", season = 1, number = 2, airdate = "2024-03-03"),
        Episode("1", "Pilot", season = 1, number = 1, airdate = "2024-02-25")
    )) }

    var isEpisodesExpanded by remember { mutableStateOf(false) }
    var selectedEpisode by remember { mutableStateOf<Episode?>(episodes.firstOrNull()) }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
    ) {
        if (selectedEpisode != null) {
            VideoPlayer(videoUrl = selectedEpisode!!.videoUrl)
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.Black))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEpisodesExpanded = !isEpisodesExpanded }
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("Episodes", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        imageVector = if (isEpisodesExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Toggle Episodes",
                        tint = Color.White,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }

            if (isEpisodesExpanded) {
                items(episodes) { episode ->
                    val isSelected = (selectedEpisode == episode)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable {
                                selectedEpisode = episode
                                // isEpisodesExpanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "S${episode.season.toString().padStart(2, '0')}E${episode.number.toString().padStart(2, '0')} • ${episode.name}",
                                color = if (isSelected) Primary else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(text = episode.airdate, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }


            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Series Details", color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Здесь будет описание сериала, актеры и прочая информация. Пока мы смотрим сериал с ID: $showId.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {  },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Add to Subscriptions", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}