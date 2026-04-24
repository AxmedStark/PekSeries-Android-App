package com.example.pekseries.ui.screen

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
import com.example.pekseries.model.Show
import com.example.pekseries.ui.theme.*
import com.example.pekseries.ui.viewmodel.HomeUiState
import com.example.pekseries.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val mainFilters = listOf("Актуальное", "Популярное", "Будущие")
    var selectedMainFilter by remember { mutableStateOf(mainFilters.first()) }

    val genresList = listOf("Жанр", "Боевик / Приключения", "Анимация / Аниме", "Комедия", "Криминал", "Документальный", "Драма", "Семейный", "Фантастика", "Детектив", "Реалити", "Ток-шоу")
    var genreExpanded by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("Жанр") }

    val typesList = listOf("Тип", "Игровой сериал", "Мини-сериал", "Документальный", "Реалити-шоу", "Ток-шоу")
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("Тип") }

    val yearsList = listOf("Год") + (2026 downTo 1990).map { it.toString() }
    var yearExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf("Год") }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Hello, Ahmad", color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onNavigateToNotifications) {
                    Icon(Icons.Filled.Notifications, "Notify", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mainFilters) { filter ->
                    FilterChip(
                        selected = selectedMainFilter == filter,
                        onClick = {
                            selectedMainFilter = filter
                            selectedGenre = "Жанр"; selectedType = "Тип"; selectedYear = "Год"
                            viewModel.loadEpisodes(filterCategory = filter)
                        },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.2f),
                            selectedLabelColor = Primary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        DropdownFilter("Жанр", selectedGenre, genresList, genreExpanded, { genreExpanded = it }) {
                            selectedGenre = it; selectedMainFilter = ""; viewModel.applyFilters(selectedGenre, selectedType, selectedYear)
                        }
                    }
                    item {
                        DropdownFilter("Тип", selectedType, typesList, typeExpanded, { typeExpanded = it }) {
                            selectedType = it; selectedMainFilter = ""; viewModel.applyFilters(selectedGenre, selectedType, selectedYear)
                        }
                    }
                    item {
                        DropdownFilter("Год", selectedYear, yearsList, yearExpanded, { yearExpanded = it }) {
                            selectedYear = it; selectedMainFilter = ""; viewModel.applyFilters(selectedGenre, selectedType, selectedYear)
                        }
                    }
                }

                if (selectedGenre != "Жанр" || selectedType != "Тип" || selectedYear != "Год") {
                    IconButton(onClick = {
                        selectedGenre = "Жанр"; selectedType = "Тип"; selectedYear = "Год"
                        selectedMainFilter = "Актуальное"
                        viewModel.loadEpisodes("Actual")
                    }) {
                        Icon(Icons.Default.Clear, "Reset", tint = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerText = if (selectedMainFilter.isNotEmpty()) selectedMainFilter else "Результаты"
                Text(headerText, color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.loadEpisodes(selectedMainFilter) }) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = Primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        when (val state = uiState) {
            is HomeUiState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is HomeUiState.Error -> item { Text(state.message, color = Primary) }
            is HomeUiState.Success -> {
                if (state.shows.isEmpty()) {
                    item { Text("Ничего не найдено 😔", color = Color.Gray) }
                } else {
                    items(state.shows) { show ->
                        HomeShowCard(show = show, onCardClick = { onNavigateToDetail(show.id) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
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
        FilterChip(
            selected = selected != placeholder,
            onClick = { onExpandedChange(true) },
            label = { Text(selected) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); onExpandedChange(false) })
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
            modifier = Modifier.size(60.dp, 80.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (show.isNew) Text("AIRED TODAY", color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(show.title, color = PekYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(show.episode ?: "", color = TextPrimary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(show.time ?: "", color = TextSecondary, fontSize = 12.sp)
        }
    }
}