package az.pekstudios.pekseries.core.domain.repository

import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.model.Show

/**
 * The signed-in user's subscriptions, stored in Firestore and mirrored to FCM
 * topics so the Cloud Function can fan out new-episode pushes.
 */
interface SubscriptionRepository {

    suspend fun getSubscribedShows(): PekResult<List<Show>>

    /** Subscribed shows that have an episode airing in the future, soonest first. */
    suspend fun getUpcomingEpisodes(): PekResult<List<Show>>

    suspend fun getSubscribedIds(): PekResult<Set<String>>

    suspend fun isSubscribed(tvMazeId: String): PekResult<Boolean>

    /** @return the new subscription state. */
    suspend fun toggleSubscription(tvMazeId: String): PekResult<Boolean>

    /** Re-subscribes FCM topics after a login, since topics are per-install. */
    suspend fun syncTopicsWithFcm(): PekResult<Unit>

    /**
     * Subscribes to or unsubscribes from every topic at once, backing the
     * profile screen's push toggle.
     */
    suspend fun setTopicsEnabled(enabled: Boolean): PekResult<Unit>
}
