package az.pekstudios.pekseries.core.domain

/**
 * Why a data operation failed, in terms the UI can act on.
 *
 * The point of enumerating these is that "no results" and "the request failed"
 * are different situations that deserve different screens. The previous
 * repository collapsed every failure into an empty list, so the UI could not
 * tell them apart and simply rendered "nothing found" when the network was down.
 */
sealed interface DataError {

    /** No connectivity, DNS failure, or timeout. Retrying later may work. */
    data object Network : DataError

    /** HTTP 429. TVMaze enforces this aggressively when requests are not batched. */
    data object RateLimited : DataError

    /** The server answered, but the thing asked for does not exist. */
    data object NotFound : DataError

    /** No signed-in user, so a per-user collection cannot be read or written. */
    data object Unauthenticated : DataError

    /** Server-side failure (5xx). */
    data object Server : DataError

    data class Unknown(val cause: Throwable? = null) : DataError

    /**
     * Sign-in specific failures. Kept in the same hierarchy rather than a
     * parallel AuthError type so there is one error vocabulary and one place
     * that turns an error into user-facing copy.
     */
    sealed interface Auth : DataError {

        /** Wrong password, or an account that does not exist. */
        data object InvalidCredentials : Auth

        data object InvalidEmail : Auth

        data object EmailAlreadyInUse : Auth

        data object WeakPassword : Auth

        data object UserDisabled : Auth

        /** Firebase asks for a recent login before this operation. */
        data object RequiresRecentLogin : Auth

        data object TooManyAttempts : Auth
    }
}
