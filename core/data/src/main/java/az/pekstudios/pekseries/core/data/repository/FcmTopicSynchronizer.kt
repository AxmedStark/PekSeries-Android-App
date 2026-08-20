package az.pekstudios.pekseries.core.data.repository

import az.pekstudios.pekseries.core.datastore.UserPreferencesDataSource
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.di.ApplicationScope
import az.pekstudios.pekseries.core.domain.repository.AuthRepository
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.domain.repository.TopicSynchronizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reconciles FCM topics whenever the user becomes signed in, and on demand when
 * the registration token changes.
 *
 * Previously the only re-subscribe path was an explicit sign-in, and its result
 * was discarded. So a token change — a reinstall being the common case — left
 * every topic orphaned with nothing to restore them and no error anywhere.
 * Reconciling on every start makes subscription state self-healing;
 * `subscribeToTopic` is idempotent, so repeating it costs nothing.
 */
@Singleton
class FcmTopicSynchronizer @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val preferences: UserPreferencesDataSource,
    @ApplicationScope private val scope: CoroutineScope,
) : TopicSynchronizer {

    private val started = AtomicBoolean(false)

    override fun start() {
        if (!started.compareAndSet(false, true)) return

        scope.launch {
            authRepository.isSignedIn
                .distinctUntilChanged()
                .collect { signedIn -> if (signedIn) reconcile() }
        }
    }

    override suspend fun reconcile() {
        if (!authRepository.isSignedInNow()) return

        // Respect the user's choice: if they turned notifications off, the
        // correct reconciled state is "unsubscribed from everything".
        val enabled = preferences.userPreferences.first().pushEnabled

        when (val result = subscriptionRepository.setTopicsEnabled(enabled)) {
            is PekResult.Success ->
                Timber.i("FCM topics reconciled (enabled=%b)", enabled)

            // Logged rather than swallowed: this failing is exactly what makes
            // notifications stop with no visible symptom.
            is PekResult.Failure ->
                Timber.w("FCM topic reconciliation failed: %s", result.error)
        }
    }
}
