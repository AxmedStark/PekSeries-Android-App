package az.pekstudios.pekseries.core.domain.repository

import az.pekstudios.pekseries.core.domain.PekResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Backed by an auth state listener, not a one-shot read of currentUser, so a
     * token expiry or a sign-out on another screen is reflected immediately.
     */
    val isSignedIn: Flow<Boolean>

    suspend fun signInWithEmail(email: String, password: String): PekResult<Unit>

    suspend fun registerWithEmail(email: String, password: String): PekResult<Unit>

    suspend fun signInWithGoogle(idToken: String): PekResult<Unit>

    suspend fun sendPasswordReset(email: String): PekResult<Unit>

    suspend fun signOut()
}
