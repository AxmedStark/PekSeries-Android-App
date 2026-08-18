package az.pekstudios.pekseries.core.testing.repository

import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.model.Show

class FakeSubscriptionRepository : SubscriptionRepository {

    var subscribedShowsResult: PekResult<List<Show>> = PekResult.Success(emptyList())
    var upcomingResult: PekResult<List<Show>> = PekResult.Success(emptyList())
    var subscribedIdsResult: PekResult<Set<String>> = PekResult.Success(emptySet())
    var isSubscribedResult: PekResult<Boolean> = PekResult.Success(false)
    var toggleResult: PekResult<Boolean> = PekResult.Success(true)
    var topicsResult: PekResult<Unit> = PekResult.Success(Unit)

    var syncTopicsCallCount = 0
    val topicsEnabledCalls = mutableListOf<Boolean>()

    override suspend fun getSubscribedShows() = subscribedShowsResult

    override suspend fun getUpcomingEpisodes() = upcomingResult

    override suspend fun getSubscribedIds() = subscribedIdsResult

    override suspend fun isSubscribed(tvMazeId: String) = isSubscribedResult

    override suspend fun toggleSubscription(tvMazeId: String) = toggleResult

    override suspend fun syncTopicsWithFcm(): PekResult<Unit> {
        syncTopicsCallCount++
        return topicsResult
    }

    override suspend fun setTopicsEnabled(enabled: Boolean): PekResult<Unit> {
        topicsEnabledCalls += enabled
        return topicsResult
    }
}
