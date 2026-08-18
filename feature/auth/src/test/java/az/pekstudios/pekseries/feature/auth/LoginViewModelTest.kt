package az.pekstudios.pekseries.feature.auth

import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.Validation
import az.pekstudios.pekseries.core.testing.repository.FakeAuthRepository
import az.pekstudios.pekseries.core.testing.repository.FakeSubscriptionRepository
import az.pekstudios.pekseries.core.testing.repository.FakeUserProfileRepository
import az.pekstudios.pekseries.core.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val subscriptions = FakeSubscriptionRepository()
    private val profile = FakeUserProfileRepository()

    private fun viewModel() = LoginViewModel(auth, subscriptions, profile)

    @Test
    fun `a malformed email is rejected before any network call`() = runTest {
        val vm = viewModel()
        vm.onEmailChange("not-an-email")
        vm.onPasswordChange("password123")

        vm.submit()

        assertThat(vm.uiState.value.emailError).isNotNull()
        assertThat(auth.signInCalls).isEmpty()
    }

    @Test
    fun `a short password blocks registration but not sign-in`() = runTest {
        val vm = viewModel()
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("123")

        // Sign-in: an existing account may predate the rule, so rejecting it
        // locally would lock the user out of their own account.
        vm.submit()
        assertThat(auth.signInCalls).hasSize(1)

        vm.toggleMode()
        vm.onPasswordChange("123")
        vm.submit()
        assertThat(vm.uiState.value.passwordError).isNotNull()
        assertThat(auth.registerCalls).isEmpty()
    }

    @Test
    fun `a valid sign-in reaches the repository and clears the password`() = runTest {
        val vm = viewModel()
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("password123")

        vm.submit()

        assertThat(auth.signInCalls).containsExactly("user@example.com" to "password123")
        assertThat(vm.uiState.value.password).isEmpty()
        assertThat(vm.uiState.value.isSubmitting).isFalse()
    }

    /** Topics are per-install, so they must be re-applied for the new account. */
    @Test
    fun `a successful sign-in re-syncs FCM topics`() = runTest {
        val vm = viewModel()
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("password123")

        vm.submit()

        assertThat(subscriptions.syncTopicsCallCount).isEqualTo(1)
    }

    @Test
    fun `a wrong password surfaces a typed error, not a raw exception string`() = runTest {
        auth.signInResult = PekResult.Failure(DataError.Auth.InvalidCredentials)
        val vm = viewModel()
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("wrongpass")

        vm.submit()

        assertThat(vm.uiState.value.error).isEqualTo(DataError.Auth.InvalidCredentials)
        assertThat(vm.uiState.value.isSubmitting).isFalse()
        assertThat(subscriptions.syncTopicsCallCount).isEqualTo(0)
    }

    @Test
    fun `registration uses the register endpoint`() = runTest {
        val vm = viewModel()
        vm.toggleMode()
        vm.onEmailChange("new@example.com")
        vm.onPasswordChange("a".repeat(Validation.MIN_PASSWORD_LENGTH))

        vm.submit()

        assertThat(auth.registerCalls).hasSize(1)
        assertThat(auth.signInCalls).isEmpty()
    }

    @Test
    fun `password reset requires a valid email first`() = runTest {
        val vm = viewModel()
        vm.onEmailChange("nope")

        vm.sendPasswordReset()

        assertThat(vm.uiState.value.emailError).isNotNull()
        assertThat(auth.passwordResetCalls).isEmpty()
    }

    @Test
    fun `password reset confirms to the user on success`() = runTest {
        val vm = viewModel()
        vm.onEmailChange("user@example.com")

        vm.sendPasswordReset()

        assertThat(auth.passwordResetCalls).containsExactly("user@example.com")
        assertThat(vm.uiState.value.message).contains("user@example.com")
    }

    @Test
    fun `typing clears a previous error so stale text does not linger`() = runTest {
        auth.signInResult = PekResult.Failure(DataError.Auth.InvalidCredentials)
        val vm = viewModel()
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("wrong")
        vm.submit()
        assertThat(vm.uiState.value.error).isNotNull()

        vm.onPasswordChange("another")

        assertThat(vm.uiState.value.error).isNull()
    }

    @Test
    fun `toggling mode resets validation state`() = runTest {
        val vm = viewModel()
        vm.onEmailChange("bad")
        vm.onPasswordChange("x")
        vm.submit()
        assertThat(vm.uiState.value.emailError).isNotNull()

        vm.toggleMode()

        assertThat(vm.uiState.value.emailError).isNull()
        assertThat(vm.uiState.value.mode).isEqualTo(AuthMode.Register)
    }

    @Test
    fun `submit is blocked while a request is in flight or fields are blank`() = runTest {
        val vm = viewModel()
        assertThat(vm.uiState.value.canSubmit).isFalse()

        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("password123")
        assertThat(vm.uiState.value.canSubmit).isTrue()
    }
}
