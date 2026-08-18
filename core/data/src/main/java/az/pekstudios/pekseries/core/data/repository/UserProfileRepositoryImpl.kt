package az.pekstudios.pekseries.core.data.repository

import az.pekstudios.pekseries.core.data.runCatchingData
import az.pekstudios.pekseries.core.datastore.UserPreferencesDataSource
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.domain.repository.UserProfileRepository
import az.pekstudios.pekseries.core.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val preferences: UserPreferencesDataSource,
    private val subscriptionRepository: SubscriptionRepository,
) : UserProfileRepository {

    /**
     * An AuthStateListener rather than a one-shot read of currentUser, so a
     * sign-out or token refresh updates the UI instead of leaving a stale name
     * on screen until the next process start.
     */
    private val authUser: Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val profile: Flow<UserProfile> =
        combine(authUser, preferences.userPreferences) { user, prefs ->
            UserProfile(
                displayName = prefs.displayNameOverride
                    ?: user?.displayName.orEmpty().ifBlank { DEFAULT_NAME },
                email = user?.email.orEmpty(),
                photoUrl = prefs.photoUriOverride ?: user?.photoUrl?.toString(),
                pushEnabled = prefs.pushEnabled,
                isSignedIn = user != null,
                hasCustomDisplayName = prefs.displayNameOverride != null,
                hasCustomPhoto = prefs.photoUriOverride != null,
            )
        }

    override suspend fun updateDisplayName(name: String?): PekResult<Unit> =
        runCatchingData("updateDisplayName") {
            preferences.setDisplayNameOverride(name)

            // Best-effort mirror to Firebase so other devices and the auth
            // console agree. The local override is the source of truth, so a
            // failure here must not fail the edit.
            runCatching {
                auth.currentUser
                    ?.updateProfile(userProfileChangeRequest { displayName = name })
                    ?.await()
            }.onFailure { Timber.w(it, "Could not mirror display name to Firebase") }

            Unit
        }

    override suspend fun updatePhotoUri(uri: String?): PekResult<Unit> =
        runCatchingData("updatePhotoUri") {
            preferences.setPhotoUriOverride(uri)
            Unit
        }

    /**
     * The toggle previously only wrote a SharedPreferences boolean that nothing
     * ever read, so turning notifications off changed nothing. Since delivery is
     * driven by FCM topics, the toggle has to subscribe/unsubscribe them.
     */
    /**
     * Writes the preference first, then reconciles FCM topics.
     *
     * The order matters for responsiveness: the switch renders from the
     * preferences Flow, so a local write moves it instantly, whereas
     * reconciling topics means a Firestore read plus one FCM round trip per
     * subscribed show and can take seconds. Doing that first made the toggle
     * appear frozen on every tap.
     *
     * If reconciliation fails the preference is rolled back, so the switch
     * cannot sit on "off" while the topics are still subscribed.
     */
    override suspend fun setPushEnabled(enabled: Boolean): PekResult<Unit> {
        preferences.setPushEnabled(enabled)

        val result = subscriptionRepository.setTopicsEnabled(enabled)
        if (result is PekResult.Failure) {
            preferences.setPushEnabled(!enabled)
        }
        return result
    }

    override suspend fun clearLocalProfile() {
        preferences.clear()
    }

    private companion object {
        const val DEFAULT_NAME = "Pek"
    }
}
