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
import com.example.pekseries.ui.theme.DarkBg
import com.example.pekseries.ui.theme.Primary
import com.example.pekseries.ui.viewmodel.DetailViewModel
import java.util.Locale

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
    val canSubscribe by viewModel.canSubscribe.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 32.dp)) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.offset(x = (-12).dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                        }

                        if (showDetails != null) {
                            Text(
                                text = showDetails!!.name,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        } else if (!isLoading) {
                            Text(
                                text = "Loading...",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        if (canSubscribe) {
                            Button(
                                onClick = { viewModel.toggleSubscription() },
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
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Tracking and subscriptions are unavailable for this show.",
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (showDetails != null) {
                            val cleanSummary = showDetails!!.overview?.replace(Regex("<.*?>"), "") ?: "No description available."
                            val rating = showDetails!!.vote_average?.let { String.format(Locale.US, "%.1f", it) + " ⭐" } ?: "No rating"
                            val genres = showDetails!!.genres?.joinToString(", ") { it.name } ?: ""

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = genres,
                                    color = Primary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = rating,
                                    color = Color.Yellow,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
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
                                    model = showDetails!!.getFullPosterUrl(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showFullScreenPoster = true },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else if (isLoading) {
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
                            contentDescription = null,
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
                    model = showDetails!!.getFullPosterUrl(),
                    contentDescription = null,
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
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}