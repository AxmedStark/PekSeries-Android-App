package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pekseries.model.Show
import com.example.pekseries.ui.theme.CardBg
import com.example.pekseries.ui.theme.DarkBg
import com.example.pekseries.ui.theme.PekYellow
import com.example.pekseries.ui.theme.Primary
import com.example.pekseries.ui.theme.TextPrimary
import com.example.pekseries.ui.theme.TextSecondary
import com.example.pekseries.ui.viewmodel.HomeUiState
import com.example.pekseries.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Главные вкладки
    val mainFilters = listOf("Актуальное", "Популярное", "Будущие")
    var selectedMainFilter by remember { mutableStateOf(mainFilters.first()) }

    // Состояния выпадающих списков
    var genreExpanded by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("Жанр") }
    val genresList = listOf("Action", "Comedy", "Drama", "Sci-Fi", "Thriller")

    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("Тип") }
    val typesList = listOf("Anime", "Animation", "Documentary")

    var yearExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf("Год") }
    val yearsList = listOf("2024", "2023", "2022", "2010s")

    LazyColumn(modifier = Modifier.padding(16.dp)) {

        // 1. ШАПКА
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Hello, Ahmad", color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                IconButton(onClick = onNavigateToNotifications) {
                    Icon(Icons.Filled.Notifications, "Notify", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. ГЛАВНЫЕ ФИЛЬТРЫ (Актуальное, Популярное, Будущие)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mainFilters) { filter ->
                    FilterChip(
                        selected = selectedMainFilter == filter,
                        onClick = {
                            selectedMainFilter = filter
                            // Сбрасываем выпадающие списки
                            selectedGenre = "Genre"; selectedType = "Type"; selectedYear = "Year"
                            viewModel.loadEpisodes(filterCategory = filter)
                        },
                        label = { Text(filter) },
                        enabled = true,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CardBg, labelColor = Color.Gray,
                            selectedContainerColor = Primary.copy(alpha = 0.2f), selectedLabelColor = Primary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selectedMainFilter == filter,
                            borderColor = Color.Transparent, selectedBorderColor = Primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. ВЫПАДАЮЩИЕ СПИСКИ (Жанр, Тип, Год)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ЖАНРЫ
                item {
                    Box {
                        FilterChip(
                            selected = selectedGenre != "Genre",
                            onClick = { genreExpanded = true },
                            label = { Text(selectedGenre) },
                            enabled = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkBg, labelColor = Color.LightGray,
                                selectedContainerColor = Primary.copy(alpha = 0.2f), selectedLabelColor = Primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = selectedGenre != "Genre",
                                borderColor = Color.Gray, selectedBorderColor = Primary
                            )
                        )
                        DropdownMenu(expanded = genreExpanded, onDismissRequest = { genreExpanded = false }) {
                            genresList.forEach { genre ->
                                DropdownMenuItem(
                                    text = { Text(genre) },
                                    onClick = {
                                        selectedGenre = genre
                                        genreExpanded = false
                                        selectedMainFilter = "" // Снимаем выделение с главных фильтров
                                        viewModel.loadEpisodes("Genre", genre)
                                    }
                                )
                            }
                        }
                    }
                }

                // ТИПЫ
                item {
                    Box {
                        FilterChip(
                            selected = selectedType != "Type",
                            onClick = { typeExpanded = true },
                            label = { Text(selectedType) },
                            enabled = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkBg, labelColor = Color.LightGray,
                                selectedContainerColor = Primary.copy(alpha = 0.2f), selectedLabelColor = Primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = selectedType != "Type",
                                borderColor = Color.Gray, selectedBorderColor = Primary
                            )
                        )
                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            typesList.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedType = type
                                        typeExpanded = false
                                        selectedMainFilter = ""
                                        viewModel.loadEpisodes("Type", type)
                                    }
                                )
                            }
                        }
                    }
                }

                // ГОД
                item {
                    Box {
                        FilterChip(
                            selected = selectedYear != "Year",
                            onClick = { yearExpanded = true },
                            label = { Text(selectedYear) },
                            enabled = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkBg, labelColor = Color.LightGray,
                                selectedContainerColor = Primary.copy(alpha = 0.2f), selectedLabelColor = Primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = selectedYear != "Year",
                                borderColor = Color.Gray, selectedBorderColor = Primary
                            )
                        )
                        DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                            yearsList.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        selectedYear = year
                                        yearExpanded = false
                                        selectedMainFilter = ""
                                        viewModel.loadEpisodes("Year", year)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. ЗАГОЛОВОК И КНОПКА ОБНОВЛЕНИЯ
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Динамический заголовок в зависимости от фильтра
                val headerText = if (selectedMainFilter.isNotEmpty()) selectedMainFilter else "$selectedGenre $selectedType $selectedYear".replace("Жанр", "").replace("Тип", "").replace("Год", "").trim()

                Text(headerText.ifEmpty { "Results" }, color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                IconButton(onClick = { viewModel.loadEpisodes(selectedMainFilter) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }
            is HomeUiState.Error -> {
                item {
                    Text(text = state.message, color = Primary)
                }
            }
            is HomeUiState.Success -> {
                if (state.shows.isEmpty()) {
                    item { Text("Сегодня ничего не вышло 😔", color = Color.Gray) }
                } else {
                    items(state.shows) { show ->
                        HomeShowCard(
                            show = show,
                            onCheckClick = { viewModel.toggleWatched(show) },
                            onCardClick = { onNavigateToDetail(show.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HomeShowCard(
    show: Show,
    onCheckClick: () -> Unit,
    onCardClick: () -> Unit
) {
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
            contentDescription = "Poster for ${show.title}",
            modifier = Modifier
                .width(60.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (show.isNew) {
                Text("AIRED TODAY", color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(show.title, color = PekYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(show.episode ?: "", color = TextPrimary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(show.time ?: "", color = TextSecondary, fontSize = 12.sp)
            }
        }
        IconButton(onClick = onCheckClick) {
            Icon(
                imageVector = if (show.isWatched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (show.isWatched) PekYellow else TextSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}