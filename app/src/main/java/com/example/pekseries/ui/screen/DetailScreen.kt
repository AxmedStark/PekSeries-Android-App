package com.example.pekseries.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pekseries.model.Episode
import com.example.pekseries.ui.component.VideoPlayer
import com.example.pekseries.ui.theme.DarkBg
import com.example.pekseries.ui.theme.Primary
import com.example.pekseries.ui.viewmodel.DetailViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    showId: String,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val episodes by viewModel.episodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val showDetails by viewModel.showDetails.collectAsState()

    var selectedEpisode by remember { mutableStateOf<Episode?>(null) }
    var showFullScreenPoster by remember { mutableStateOf(false) }

    LaunchedEffect(episodes) {
        if (episodes.isNotEmpty() && selectedEpisode == null) {
            selectedEpisode = episodes.first()
        }
    }

    LaunchedEffect(showId) {
        viewModel.loadEpisodes(showId)
    }

    var isEpisodesExpanded by remember { mutableStateOf(false) }
    var visibleEpisodesCount by remember { mutableStateOf(10) }
    val visibleEpisodes = episodes.take(visibleEpisodesCount)

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {

            // 1. ПЛЕЕР
            if (selectedEpisode != null) {
                VideoPlayer(videoUrl = selectedEpisode!!.videoUrl)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.Black)) {
                    if (isLoading) CircularProgressIndicator(color = Primary, modifier = Modifier.align(Alignment.Center))
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Text("Series Info", color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.toggleSubscription(showId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed) Color.DarkGray else Primary,
                                contentColor = if (isSubscribed) Primary else DarkBg
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(
                                text = if (isSubscribed) "Remove from Subscriptions" else "Add to Subscriptions",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (showDetails != null) {
                            val cleanSummary = showDetails!!.summary?.replace(Regex("<.*?>"), "") ?: "No description available."
                            val rating = showDetails!!.rating?.average?.let { "$it ⭐" } ?: "No rating"
                            val genres = showDetails!!.genres?.joinToString(", ") ?: ""

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = genres, color = Primary, fontSize = 14.sp)
                                Text(text = rating, color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = cleanSummary,
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 16.dp)
                                )

                                AsyncImage(
                                    model = showDetails!!.image?.medium ?: showDetails!!.image?.original,
                                    contentDescription = "Poster",
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showFullScreenPoster = true }, // Открываем на весь экран
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            LinearProgressIndicator(color = Primary, modifier = Modifier.fillMaxWidth())
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBg)
                            .clickable {
                                isEpisodesExpanded = !isEpisodesExpanded
                                if (!isEpisodesExpanded) visibleEpisodesCount = 10
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Episodes (${episodes.size})", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (isEpisodesExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = Color.White
                        )
                    }
                }

                if (isEpisodesExpanded) {
                    if (episodes.isEmpty() && !isLoading) {
                        item { Text("No episodes found", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                    } else {
                        items(visibleEpisodes) { episode ->
                            val isSelected = (selectedEpisode == episode)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedEpisode = episode }
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
                                Text(text = episode.airdate ?: "TBA", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        if (visibleEpisodesCount < episodes.size) {
                            item {
                                TextButton(
                                    onClick = { visibleEpisodesCount += 10 },
                                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                                ) {
                                    Text("Show more (${episodes.size - visibleEpisodesCount})", color = Primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showFullScreenPoster && showDetails != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = showDetails!!.image?.original ?: showDetails!!.image?.medium,
                    contentDescription = "Full Screen Poster",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { showFullScreenPoster = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 32.dp, start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}