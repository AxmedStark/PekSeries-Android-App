package com.example.pekseries.data.remote

import com.example.pekseries.model.Episode
import com.example.pekseries.model.SearchResponseItem
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TvMazeApi {
    @GET("schedule")
    suspend fun getSchedule(
        @Query("country") country: String = "US",
        @Query("date") date: String
    ): List<TvMazeEpisodeDto>

    @GET("shows/{id}/episodes")
    suspend fun getShowEpisodes(@Path("id") showId: String): List<Episode>

    @GET("search/shows")
    suspend fun searchSeries(
        @Query("q") query: String
    ): List<SearchResponseItem>

    @GET("shows/{id}")
    suspend fun getShowById(@Path("id") showId: String): TvMazeShowDto
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
    val image: TvMazeImageDto?,
    val summary: String? = null,
    val genres: List<String>? = null,
    val rating: TvMazeRatingDto? = null,
    val premiered: String? = null,
    val weight: Int? = 0,
    val type: String? = null
)
//
data class TvMazeRatingDto(
    val average: Double?,
    val tempvar: String?
)

data class TvMazeImageDto(
    val medium: String?,
    val original: String?
)
