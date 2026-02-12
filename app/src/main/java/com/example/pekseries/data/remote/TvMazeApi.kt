package com.example.pekseries.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// Интерфейс для запросов
interface TvMazeApi {
    @GET("schedule")
    suspend fun getSchedule(
        @Query("country") country: String = "US",
        @Query("date") date: String // Формат YYYY-MM-DD
    ): List<TvMazeEpisodeDto>
}

// --- DTO (Data Transfer Objects) - чистые данные из JSON ---

data class TvMazeEpisodeDto(
    val id: Int,
    val name: String,
    val season: Int,
    val number: Int,
    val airdate: String,
    val airtime: String,
    val show: TvMazeShowDto
)

data class TvMazeShowDto(
    val id: Int,
    val name: String,
    val image: TvMazeImageDto?
)

data class TvMazeImageDto(
    val medium: String?,
    val original: String?
)