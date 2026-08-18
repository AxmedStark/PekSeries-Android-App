package az.pekstudios.pekseries.core.domain.repository

import az.pekstudios.pekseries.core.domain.PekResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Backed by an auth state listener, not a one-shot read of currentUser, so a
     * token expiry or a sign-out on another screen is reflected immediately.
     */
    val isSignedIn: Flow<Boolean>

    /**
     * The state that is already known synchronously, used as the initial value
     * of the UI's StateFlow.
     *
     * Without it the flow starts at `false` and the login screen is rendered for
     * a frame or two on every launch before the auth listener reports the real
     * state - a visible flash for an already-signed-in user.
     */
    fun isSignedInNow(): Boolean

    suspend fun signInWithEmail(email: String, password: String): PekResult<Unit>

    suspend fun registerWithEmail(email: String, password: String): PekResult<Unit>

    suspend fun signInWithGoogle(idToken: String): PekResult<Unit>

    suspend fun sendPasswordReset(email: String): PekResult<Unit>

    suspend fun signOut()
}
