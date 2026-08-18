package az.pekstudios.pekseries.core.data.repository

import az.pekstudios.pekseries.core.data.runCatchingData
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthRepository {

    override val isSignedIn: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser != null) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override suspend fun signInWithEmail(email: String, password: String): PekResult<Unit> =
        runCatchingData("signInWithEmail") {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            Unit
        }

    override suspend fun registerWithEmail(email: String, password: String): PekResult<Unit> =
        runCatchingData("registerWithEmail") {
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
            // Best effort: a failure to send verification must not fail signup.
            runCatching { auth.currentUser?.sendEmailVerification()?.await() }
            Unit
        }

    override suspend fun signInWithGoogle(idToken: String): PekResult<Unit> =
        runCatchingData("signInWithGoogle") {
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
            Unit
        }

    override suspend fun sendPasswordReset(email: String): PekResult<Unit> =
        runCatchingData("sendPasswordReset") {
            auth.sendPasswordResetEmail(email.trim()).await()
            Unit
        }

    override suspend fun signOut() {
        auth.signOut()
    }
}
