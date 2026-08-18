package az.pekstudios.pekseries.core.testing.repository

import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {

    private val signedIn = MutableStateFlow(false)
    override val isSignedIn: Flow<Boolean> = signedIn

    override fun isSignedInNow(): Boolean = signedIn.value

    fun setSignedIn(value: Boolean) { signedIn.value = value }

    var signInResult: PekResult<Unit> = PekResult.Success(Unit)
    var registerResult: PekResult<Unit> = PekResult.Success(Unit)
    var googleResult: PekResult<Unit> = PekResult.Success(Unit)
    var passwordResetResult: PekResult<Unit> = PekResult.Success(Unit)

    val signInCalls = mutableListOf<Pair<String, String>>()
    val registerCalls = mutableListOf<Pair<String, String>>()
    val passwordResetCalls = mutableListOf<String>()
    var signOutCallCount = 0

    override suspend fun signInWithEmail(email: String, password: String): PekResult<Unit> {
        signInCalls += email to password
        if (signInResult is PekResult.Success) signedIn.value = true
        return signInResult
    }

    override suspend fun registerWithEmail(email: String, password: String): PekResult<Unit> {
        registerCalls += email to password
        if (registerResult is PekResult.Success) signedIn.value = true
        return registerResult
    }

    override suspend fun signInWithGoogle(idToken: String): PekResult<Unit> {
        if (googleResult is PekResult.Success) signedIn.value = true
        return googleResult
    }

    override suspend fun sendPasswordReset(email: String): PekResult<Unit> {
        passwordResetCalls += email
        return passwordResetResult
    }

    override suspend fun signOut() {
        signOutCallCount++
        signedIn.value = false
    }
}
