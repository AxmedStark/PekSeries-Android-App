package az.pekstudios.pekseries.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import az.pekstudios.pekseries.core.model.Show
import az.pekstudios.pekseries.core.ui.component.PekEmptyView
import az.pekstudios.pekseries.core.ui.component.PekErrorView
import az.pekstudios.pekseries.core.ui.component.PekLoadingView
import az.pekstudios.pekseries.core.ui.theme.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"
    val photoUrl = currentUser?.photoUrl

    val mainFilters = listOf("Airing Now", "Popular", "Upcoming")
    var selectedMainFilter by remember { mutableStateOf(mainFilters.first()) }

    // Состояния фильтров
    val genresList = listOf("Genre", "Action & Adventure", "Animation", "Comedy", "Crime", "Documentary", "Drama", "Family", "Sci-Fi & Fantasy", "Mystery", "Reality", "Talk", "Western")
    var genreExpanded by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("Genre") }

    val typesList = listOf("Type", "Scripted", "Miniseries", "Documentary", "Reality", "Talk Show", "News")
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("Type") }

    val yearsList = listOf("Year") + (2026 downTo 1990).map { it.toString() }
    var yearExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf("Year") }

    // Состояния шторки
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isAnyFilterApplied = selectedGenre != "Genre" || selectedType != "Type" || selectedYear != "Year"
    val isRefreshing = uiState is HomeUiState.Loading

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = PekDarkBg,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Filters", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                DropdownFilter("Genre", selectedGenre, genresList, genreExpanded, { genreExpanded = it }) {
                    selectedGenre = it; selectedMainFilter = ""; viewModel.applyFilters(selectedGenre, selectedType, selectedYear)
                }
                Spacer(modifier = Modifier.height(8.dp))
                DropdownFilter("Type", selectedType, typesList, typeExpanded, { typeExpanded = it }) {
                    selectedType = it; selectedMainFilter = ""; viewModel.applyFilters(selectedGenre, selectedType, selectedYear)
                }
                Spacer(modifier = Modifier.height(8.dp))
                DropdownFilter("Year", selectedYear, yearsList, yearExpanded, { yearExpanded = it }) {
                    selectedYear = it; selectedMainFilter = ""; viewModel.applyFilters(selectedGenre, selectedType, selectedYear)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false } },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Apply", color = Color.White) }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadEpisodes(if (selectedMainFilter.isNotEmpty()) selectedMainFilter else "Airing Now") },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.DarkGray).clickable { onNavigateToProfile() }, contentAlignment = Alignment.Center) {
                            if (photoUrl != null) AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Icon(Icons.Filled.Person, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Hello, $userName", color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Filled.Notifications, null, tint = Color.White) }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(mainFilters) { filter ->
                        FilterChip(
                            selected = selectedMainFilter == filter,
                            onClick = { selectedMainFilter = filter; viewModel.loadEpisodes(filterCategory = filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.2f), selectedLabelColor = Primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Results", color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.FilterList, null, tint = if (isAnyFilterApplied) PekYellow else Primary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            // Exhaustive on purpose: an `else -> Unit` here previously meant a
            // failed load rendered a blank screen with no way to retry.
            when (val state = uiState) {
                is HomeUiState.Success ->
                    items(state.shows) {
                        HomeShowCard(it) { onNavigateToDetail(it.id) }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                HomeUiState.Loading -> item { PekLoadingView() }

                HomeUiState.Empty -> item {
                    PekEmptyView("No shows to show here right now.")
                }

                is HomeUiState.Error -> item {
                    PekErrorView(error = state.error, onRetry = viewModel::retry)
                }
            }
        }
    }
}

@Composable
fun DropdownFilter(
    placeholder: String,
    selected: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    Box {
        OutlinedButton(onClick = { onExpandedChange(true) }, modifier = Modifier.fillMaxWidth()) {
            Text(selected, color = Color.White)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(CardBg)
        ) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option, color = Color.White) }, onClick = { onSelect(option); onExpandedChange(false) })
            }
        }
    }
}

@Composable
fun HomeShowCard(show: Show, onCardClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .clickable { onCardClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = show.getPosterUrl(),
            contentDescription = null,
            modifier = Modifier.size(60.dp, 80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(show.title, color = PekYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(show.episode ?: "", color = TextPrimary, fontSize = 12.sp)
            //Spacer(modifier = Modifier.height(4.dp))
            Text(show.time ?: "", color = TextSecondary, fontSize = 12.sp)
        }
        if (show.isSubscribed) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF03DAC5),
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
        }
    }
}