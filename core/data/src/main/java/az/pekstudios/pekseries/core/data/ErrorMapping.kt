package az.pekstudios.pekseries.core.data

import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import com.google.firebase.firestore.FirebaseFirestoreException
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/** Translates the exceptions our data sources throw into typed [DataError]s. */
fun Throwable.toDataError(): DataError = when (this) {
    is HttpException -> when (code()) {
        401, 403 -> DataError.Unauthenticated
        404 -> DataError.NotFound
        429 -> DataError.RateLimited
        in 500..599 -> DataError.Server
        else -> DataError.Unknown(this)
    }

    is NotSignedInException -> DataError.Unauthenticated

    is SocketTimeoutException, is IOException -> DataError.Network

    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.UNAUTHENTICATED,
        FirebaseFirestoreException.Code.PERMISSION_DENIED,
        -> DataError.Unauthenticated

        FirebaseFirestoreException.Code.NOT_FOUND -> DataError.NotFound
        FirebaseFirestoreException.Code.UNAVAILABLE -> DataError.Network
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> DataError.RateLimited
        else -> DataError.Unknown(this)
    }

    else -> DataError.Unknown(this)
}

/**
 * Runs [block], converting a thrown exception into a typed failure.
 *
 * CancellationException is deliberately rethrown: swallowing it breaks
 * structured concurrency, so a cancelled screen would keep its coroutines alive.
 * The old repository caught `Exception` everywhere and had exactly this bug.
 */
suspend inline fun <T> runCatchingData(
    tag: String,
    crossinline block: suspend () -> T,
): PekResult<T> = try {
    PekResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    val error = throwable.toDataError()
    Timber.w(throwable, "%s failed: %s", tag, error)
    PekResult.Failure(error)
}
