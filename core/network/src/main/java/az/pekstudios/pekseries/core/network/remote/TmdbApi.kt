package az.pekstudios.pekseries.core.network.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("tv/airing_today")
    suspend fun getAiringToday(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TmdbResponse

    @GET("trending/tv/day")
    suspend fun getTrending(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TmdbResponse

    @GET("tv/on_the_air")
    suspend fun getOnTheAir(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TmdbResponse

    @GET("discover/tv")
    suspend fun discoverShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("with_genres") genreIds: String? = null,
        @Query("first_air_date_year") year: String? = null,
        @Query("with_type") type: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbResponse

    @GET("search/tv")
    suspend fun searchSeries(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US"
    ): TmdbResponse

    @GET("tv/{series_id}")
    suspend fun getShowDetails(
        @Path("series_id") showId: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "videos,external_ids"
    ): TmdbShowDetailDto

    @GET("tv/{series_id}/season/{season_number}")
    suspend fun getSeasonEpisodes(
        @Path("series_id") showId: String,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TmdbSeasonDto

    @GET("find/{external_id}")
    suspend fun findByExternalId(
        @Path("external_id") externalId: String,
        @Query("api_key") apiKey: String,
        @Query("external_source") externalSource: String = "imdb_id"
    ): TmdbFindResponse
}

data class TmdbResponse(
    val results: List<TmdbShowDto>
)

data class TmdbShowDto(
    val id: Int,
    val name: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val first_air_date: String?,
    val vote_average: Double?,
    val genre_ids: List<Int>?
) {
    fun getFullPosterUrl(): String {
        return if (poster_path != null) "https://image.tmdb.org/t/p/w500$poster_path" else ""
    }
}

data class TmdbShowDetailDto(
    val id: Int,
    val name: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double?,
    val genres: List<TmdbGenreDto>?,
    val videos: TmdbVideosResponse?,
    val external_ids: TmdbExternalIdsDto?
) {
    fun getFullPosterUrl() = if (poster_path != null) "https://image.tmdb.org/t/p/w500$poster_path" else ""
}

data class TmdbGenreDto(val id: Int, val name: String)

data class TmdbVideosResponse(val results: List<TmdbVideoDto>)

data class TmdbFindResponse(val tv_results: List<TmdbShowDto>)

data class TmdbVideoDto(val key: String, val site: String, val type: String)

data class TmdbExternalIdsDto(val imdb_id: String?, val tvdb_id: Int?)

data class TmdbSeasonDto(val episodes: List<TmdbEpisodeDetailDto>)

data class TmdbEpisodeDetailDto(
    val id: Int,
    val name: String,
    val episode_number: Int,
    val season_number: Int,
    val air_date: String?
)