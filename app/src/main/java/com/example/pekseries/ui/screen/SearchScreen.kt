package com.example.pekseries.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pekseries.model.TrendingItem
import com.example.pekseries.ui.theme.*

@Composable
fun SearchScreen() {
    val categories = listOf("Trending", "Action", "Comedy", "Anime", "Drama")
    val trending = listOf(
        TrendingItem("Neon Nights", Color.Cyan),
        TrendingItem("Shadow Realm", Color.Green),
        TrendingItem("Golden Throne", Color.Yellow),
        TrendingItem("Spirit Walker", Color.Blue)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Find movies & series...") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow {
            items(categories) { cat ->
                val isSelected = cat == "Trending"
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Red else CardBg),
                    modifier = Modifier.padding(end = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(cat, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text("Trending Now", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(trending) { item ->
                TrendingCard(item)
            }
        }
    }
}

@Composable
fun TrendingCard(item: TrendingItem) {
    Box(
        modifier = Modifier
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))

        Box(modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(Red),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Text(
            item.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        )
    }
}