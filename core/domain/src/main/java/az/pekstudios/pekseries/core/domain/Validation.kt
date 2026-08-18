package az.pekstudios.pekseries.core.domain

/**
 * Input rules shared by sign-in and registration.
 *
 * Validating before the network call means an obviously bad address gets an
 * instant, specific message instead of a round-trip and a generic Firebase error.
 */
object Validation {
    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s.]+\\.[^@\\s]+$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH
}
