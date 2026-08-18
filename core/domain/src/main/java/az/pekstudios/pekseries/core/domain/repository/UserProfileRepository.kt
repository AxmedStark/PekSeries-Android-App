package az.pekstudios.pekseries.core.domain.repository

import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in user's identity and local preferences.
 *
 * Display name and photo come from the auth provider by default, but a local
 * override wins when the user edits their profile. Storing the override locally
 * means it survives re-login without depending on the provider accepting a
 * profile update.
 */
interface UserProfileRepository {

    val profile: Flow<UserProfile>

    /** Passing null clears the override and restores the provider's value. */
    suspend fun updateDisplayName(name: String?): PekResult<Unit>

    suspend fun updatePhotoUri(uri: String?): PekResult<Unit>

    /** Also subscribes or unsubscribes every FCM topic, not just the flag. */
    suspend fun setPushEnabled(enabled: Boolean): PekResult<Unit>

    /** Clears local overrides so the next account does not inherit them. */
    suspend fun clearLocalProfile()
}
