package az.pekstudios.pekseries.core.data.repository

import az.pekstudios.pekseries.core.data.mapper.toShow
import az.pekstudios.pekseries.core.data.mapper.toUpcomingShow
import az.pekstudios.pekseries.core.data.NotSignedInException
import az.pekstudios.pekseries.core.data.runCatchingData
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.model.Show
import az.pekstudios.pekseries.core.network.remote.TvMazeApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val tvMazeApi: TvMazeApi,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging,
) : SubscriptionRepository {

    override suspend fun getSubscribedIds(): PekResult<Set<String>> =
        withSubscriptions { snapshot -> snapshot.map { it.id }.toSet() }

    override suspend fun getSubscribedShows(): PekResult<List<Show>> =
        withSubscriptions { docs ->
            coroutineScope {
                docs.map { doc ->
                    async {
                        // One bad id must not fail the whole watchlist.
                        runCatching { tvMazeApi.getShowById(doc.id).toShow(isSubscribed = true) }
                            .getOrNull()
                    }
                }.awaitAll().filterNotNull()
            }
        }

    override suspend fun getUpcomingEpisodes(): PekResult<List<Show>> =
        withSubscriptions { docs ->
            val now = Instant.now()

            // Batched so a large watchlist does not trip TVMaze's rate limit.
            val shows = docs.map { it.id }.chunked(REQUEST_CHUNK_SIZE).flatMap { chunk ->
                coroutineScope {
                    chunk.map { id ->
                        async {
                            runCatching {
                                val show = tvMazeApi.getShowById(id)
                                val next = tvMazeApi.getShowEpisodes(id).firstOrNull { episode ->
                                    episode.airstamp?.let { Instant.parse(it).isAfter(now) } == true
                                } ?: return@runCatching null

                                show.toUpcomingShow(
                                    season = next.season,
                                    number = next.number,
                                    episodeName = next.name,
                                    airstamp = Instant.parse(next.airstamp),
                                )
                            }.getOrNull()
                        }
                    }.awaitAll()
                }
            }

            shows.filterNotNull().sortedBy { it.airTimeMs }
        }

    override suspend fun isSubscribed(tvMazeId: String): PekResult<Boolean> =
        runCatchingData("isSubscribed") {
            subscriptionsOrThrow().document(tvMazeId).get().await().exists()
        }

    override suspend fun toggleSubscription(tvMazeId: String): PekResult<Boolean> =
        runCatchingData("toggleSubscription") {
            val document = subscriptionsOrThrow().document(tvMazeId)

            if (document.get().await().exists()) {
                document.delete().await()
                messaging.unsubscribeFromTopic(topicFor(tvMazeId)).await()
                false
            } else {
                document.set(
                    mapOf(
                        "showId" to tvMazeId,
                        "addedAt" to System.currentTimeMillis(),
                    ),
                ).await()
                messaging.subscribeToTopic(topicFor(tvMazeId)).await()
                true
            }
        }

    override suspend fun syncTopicsWithFcm(): PekResult<Unit> = setTopicsEnabled(enabled = true)

    /**
     * FCM topic subscriptions live on the install, not the account, so they have
     * to be re-applied after a login and can be dropped wholesale when the user
     * turns notifications off.
     */
    override suspend fun setTopicsEnabled(enabled: Boolean): PekResult<Unit> =
        runCatchingData("setTopicsEnabled") {
            val ids = subscriptionsOrThrow().get().await().map { it.id }

            coroutineScope {
                ids.map { id ->
                    async {
                        val topic = topicFor(id)
                        if (enabled) {
                            messaging.subscribeToTopic(topic).await()
                        } else {
                            messaging.unsubscribeFromTopic(topic).await()
                        }
                    }
                }.awaitAll()
            }
        }

    private fun subscriptionsOrThrow() =
        firestore.collection(USERS).document(currentUserIdOrThrow()).collection(SUBSCRIPTIONS)

    private fun currentUserIdOrThrow(): String =
        auth.currentUser?.uid ?: throw NotSignedInException()

    /**
     * Shared shape for the "read the user's subscriptions" calls. Signing out is
     * reported as [DataError.Unauthenticated] rather than an empty list, so the
     * UI can prompt for login instead of claiming the watchlist is empty.
     */
    private suspend inline fun <T> withSubscriptions(
        crossinline transform: suspend (List<com.google.firebase.firestore.DocumentSnapshot>) -> T,
    ): PekResult<T> = runCatchingData("subscriptions") {
        transform(subscriptionsOrThrow().get().await().documents)
    }

    private fun topicFor(tvMazeId: String) = "$TOPIC_PREFIX$tvMazeId"


    private companion object {
        const val USERS = "users"
        const val SUBSCRIPTIONS = "subscriptions"
        const val TOPIC_PREFIX = "show_"
        const val REQUEST_CHUNK_SIZE = 5
    }
}
