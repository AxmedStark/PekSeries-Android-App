package az.pekstudios.pekseries.core.network.remote

import az.pekstudios.pekseries.core.model.Episode
import az.pekstudios.pekseries.core.model.SearchResponseItem
import az.pekstudios.pekseries.core.model.TvMazeEpisodeDto
import az.pekstudios.pekseries.core.model.TvMazeShowDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TvMazeApi {

    @GET("lookup/shows")
    suspend fun getTvMazeShowByImdb(@Query("imdb") imdbId: String): TvMazeShowDto

    @GET("lookup/shows")
    suspend fun getTvMazeShowByTvdb(@Query("thetvdb") tvdbId: Int): TvMazeShowDto

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
