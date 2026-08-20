package az.pekstudios.pekseries.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.Validation
import az.pekstudios.pekseries.core.domain.onFailure
import az.pekstudios.pekseries.core.domain.repository.AuthRepository
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import az.pekstudios.pekseries.core.domain.repository.UserProfileRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class AuthMode { SignIn, Register }

data class LoginUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSubmitting: Boolean = false,
    val error: DataError? = null,
    val message: String? = null,
) {
    val canSubmit: Boolean get() = !isSubmitting && email.isNotBlank() && password.isNotBlank()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    /**
     * Driven by an auth state listener. The previous implementation read
     * auth.currentUser once at construction, so the flag could drift from
     * reality after a token expiry.
     */
    val isUserLoggedIn: StateFlow<Boolean> = authRepository.isSignedIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            // Seeded synchronously: starting at false rendered the login
            // screen for a moment on every cold start.
            initialValue = authRepository.isSignedInNow(),
        )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, error = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, error = null) }

    fun toggleMode() = _uiState.update {
        it.copy(
            mode = if (it.mode == AuthMode.SignIn) AuthMode.Register else AuthMode.SignIn,
            emailError = null,
            passwordError = null,
            error = null,
            message = null,
        )
    }

    fun submit() {
        val state = _uiState.value
        if (!validate(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, message = null) }

            val result = when (state.mode) {
                AuthMode.SignIn -> authRepository.signInWithEmail(state.email, state.password)
                AuthMode.Register -> authRepository.registerWithEmail(state.email, state.password)
            }
            handleAuthResult(result)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            handleAuthResult(authRepository.signInWithGoogle(idToken))
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email
        if (!Validation.isValidEmail(email)) {
            _uiState.update { it.copy(emailError = "Enter your email address first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, message = null) }

            when (val result = authRepository.sendPasswordReset(email)) {
                is PekResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, message = "Password reset link sent to $email.")
                }

                is PekResult.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.error)
                }
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            authRepository.signOut()

            // Local name/photo overrides must not carry over to the next account.
            userProfileRepository.clearLocalProfile()

            // Clears the cached Google account so the picker appears again
            // rather than silently reusing the last one.
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(context, options).signOut()

            _uiState.value = LoginUiState()
        }
    }

    fun dismissMessage() = _uiState.update { it.copy(error = null, message = null) }

    private fun validate(state: LoginUiState): Boolean {
        val emailError = if (!Validation.isValidEmail(state.email)) {
            "Enter a valid email address."
        } else {
            null
        }

        // Only enforced on registration: an existing account may predate the
        // rule, and rejecting its password locally would lock the user out.
        val passwordError = if (
            state.mode == AuthMode.Register && !Validation.isValidPassword(state.password)
        ) {
            "Use at least ${Validation.MIN_PASSWORD_LENGTH} characters."
        } else {
            null
        }

        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
        return emailError == null && passwordError == null
    }

    private suspend fun handleAuthResult(result: PekResult<Unit>) {
        when (result) {
            is PekResult.Success -> {
                // FCM topics live on the install, so they must be re-applied for
                // whoever just signed in. The result was previously discarded,
                // which meant a failure here ended push delivery silently.
                subscriptionRepository.syncTopicsWithFcm()
                    .onFailure { Timber.w("Topic sync after sign-in failed: %s", it) }

                _uiState.update { it.copy(isSubmitting = false, password = "") }
            }

            is PekResult.Failure -> _uiState.update {
                it.copy(isSubmitting = false, error = result.error)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
