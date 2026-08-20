package az.pekstudios.pekseries.core.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import az.pekstudios.pekseries.core.datastore.UserPreferencesDataSource
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.testing.repository.FakeAuthRepository
import az.pekstudios.pekseries.core.testing.repository.FakeSubscriptionRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Regression cover for the defect that stopped push delivery entirely:
 * FCM binds topic subscriptions to the registration token, a reinstall issues a
 * new token, and nothing re-subscribed because the only path ran at sign-in.
 */
class FcmTopicSynchronizerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val auth = FakeAuthRepository()
    private val subscriptions = FakeSubscriptionRepository()

    private fun prefs(scope: CoroutineScope) = UserPreferencesDataSource(
        PreferenceDataStoreFactory.create(scope = scope) {
            tempFolder.newFile("prefs_${System.nanoTime()}.preferences_pb")
        },
    )

    private fun synchronizer(scope: CoroutineScope) = FcmTopicSynchronizer(
        authRepository = auth,
        subscriptionRepository = subscriptions,
        preferences = prefs(scope),
        scope = scope,
    )

    @Test
    fun `reconcile re-subscribes every topic for a signed-in user`() =
        runTest(UnconfinedTestDispatcher()) {
            auth.setSignedIn(true)

            synchronizer(backgroundScope).reconcile()

            assertThat(subscriptions.topicsEnabledCalls).containsExactly(true)
        }

    @Test
    fun `reconcile does nothing while signed out`() = runTest(UnconfinedTestDispatcher()) {
        auth.setSignedIn(false)

        synchronizer(backgroundScope).reconcile()

        assertThat(subscriptions.topicsEnabledCalls).isEmpty()
    }

    /** Turning notifications off must survive a reconcile, not be undone by it. */
    @Test
    fun `reconcile honours a disabled push preference`() = runTest(UnconfinedTestDispatcher()) {
        auth.setSignedIn(true)
        val preferences = prefs(backgroundScope)
        preferences.setPushEnabled(false)

        FcmTopicSynchronizer(auth, subscriptions, preferences, backgroundScope).reconcile()

        assertThat(subscriptions.topicsEnabledCalls).containsExactly(false)
    }

    @Test
    fun `a reconcile failure is reported rather than thrown`() =
        runTest(UnconfinedTestDispatcher()) {
            auth.setSignedIn(true)
            subscriptions.topicsResult = PekResult.Failure(DataError.Network)

            // Must not blow up the caller: this runs from Application.onCreate
            // and a Service callback.
            synchronizer(backgroundScope).reconcile()

            assertThat(subscriptions.topicsEnabledCalls).hasSize(1)
        }

    /**
     * The core fix: becoming signed in triggers reconciliation on its own, so a
     * fresh install restores subscriptions without needing an explicit sign-in
     * to happen to succeed.
     */
    @Test
    fun `start reconciles as soon as the user is signed in`() =
        runTest(UnconfinedTestDispatcher()) {
            auth.setSignedIn(false)
            val sync = synchronizer(backgroundScope)

            sync.start()
            assertThat(subscriptions.topicsEnabledCalls).isEmpty()

            auth.setSignedIn(true)

            assertThat(subscriptions.topicsEnabledCalls).containsExactly(true)
        }

    @Test
    fun `start is idempotent`() = runTest(UnconfinedTestDispatcher()) {
        auth.setSignedIn(true)
        val sync = synchronizer(backgroundScope)

        sync.start()
        sync.start()
        sync.start()

        assertThat(subscriptions.topicsEnabledCalls).containsExactly(true)
    }
}
