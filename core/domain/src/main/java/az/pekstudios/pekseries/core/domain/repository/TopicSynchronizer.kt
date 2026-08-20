package az.pekstudios.pekseries.core.domain.repository

/**
 * Keeps FCM topic subscriptions in step with the user's Firestore watchlist.
 *
 * FCM binds topic subscriptions to the *registration token*, not to the account.
 * A new token — from a reinstall, cleared app data, or a periodic rotation —
 * starts with no subscriptions at all, and FCM does not migrate them. Unless
 * something re-subscribes, pushes stop silently and permanently.
 */
interface TopicSynchronizer {

    /** Starts observing auth state; safe to call more than once. */
    fun start()

    /** Re-applies every topic subscription for the signed-in user. */
    suspend fun reconcile()
}
