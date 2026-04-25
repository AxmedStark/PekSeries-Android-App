package com.example.pekseries.ui.screen

import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pekseries.model.Episode
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
    val trailerKey by viewModel.trailerKey.collectAsState()

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            if (trailerKey != null) {
                YouTubeTrailerPlayer(videoId = trailerKey!!)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.Black)) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.align(Alignment.Center))
                    } else {
                        Text("Трейлер не найден \uD83D\uDE14", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    }
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
                                    text = "Отслеживание серий и подписка недоступны для этого шоу (Нет в базе трекинга)",
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (showDetails != null) {
                            val cleanSummary = showDetails!!.overview?.replace(Regex("<.*?>"), "") ?: "No description available."
                            val rating = showDetails!!.vote_average?.let { "$it ⭐" } ?: "No rating"
                            val genres = showDetails!!.genres?.joinToString(", ") { it.name } ?: ""

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
                                    model = showDetails!!.getFullPosterUrl(),
                                    contentDescription = "Poster",
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showFullScreenPoster = true },
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
                    model = showDetails!!.getFullPosterUrl(),
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

@Composable
fun YouTubeTrailerPlayer(videoId: String) {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()

                val html = """
                    <html>
                    <body style="margin:0;padding:0;background:black;">
                        <iframe width="100%" height="100%" src="https://www.youtube.com/embed/$videoId" frameborder="0" allow="autoplay; fullscreen" allowfullscreen></iframe>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
            }
        }
    )
}