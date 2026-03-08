package com.example.pekseries.data.remote

import com.example.pekseries.model.SearchResponseItem
import retrofit2.http.GET
import retrofit2.http.Query

interface TvMazeApi {
    @GET("schedule")
    suspend fun getSchedule(
        @Query("country") country: String = "US",
        @Query("date") date: String
    ): List<TvMazeEpisodeDto>

    @GET("search/shows")
    suspend fun searchSeries(
        @Query("q") query: String
    ): List<SearchResponseItem>
}

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
