package az.pekstudios.pekseries.core.domain.repository

import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.model.Episode
import az.pekstudios.pekseries.core.model.Show
import az.pekstudios.pekseries.core.model.ShowDetails

/** Read-only catalogue data, sourced from TMDB and TVMaze. */
interface ShowRepository {

    suspend fun getAiringToday(): PekResult<List<Show>>

    suspend fun getTrending(): PekResult<List<Show>>

    suspend fun getUpcomingPremieres(): PekResult<List<Show>>

    suspend fun discover(
        genreId: String?,
        year: String?,
        typeId: String?,
    ): PekResult<List<Show>>

    suspend fun search(query: String): PekResult<List<Show>>

    suspend fun getShowDetails(tmdbId: String): PekResult<ShowDetails>

    suspend fun getEpisodes(tvMazeId: String): PekResult<List<Episode>>

    /**
     * TMDB and TVMaze use different ids, and only TVMaze exposes an episode
     * schedule, so a TMDB id has to be translated before episodes or
     * subscriptions are available.
     */
    suspend fun findTvMazeId(details: ShowDetails): PekResult<String>

    suspend fun findTmdbId(tvMazeId: String): PekResult<String>
}
