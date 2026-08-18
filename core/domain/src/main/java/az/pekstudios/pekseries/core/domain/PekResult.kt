package az.pekstudios.pekseries.core.domain

/**
 * The outcome of a data operation: either a value, or a typed reason it failed.
 *
 * Repositories return this instead of throwing, so that a caller cannot forget
 * to handle failure, and instead of returning bare empty lists, so that "no
 * data" stays distinguishable from "the call failed".
 */
sealed interface PekResult<out T> {

    data class Success<out T>(val data: T) : PekResult<T>

    data class Failure(val error: DataError) : PekResult<Nothing>
}

inline fun <T, R> PekResult<T>.map(transform: (T) -> R): PekResult<R> = when (this) {
    is PekResult.Success -> PekResult.Success(transform(data))
    is PekResult.Failure -> this
}

inline fun <T> PekResult<T>.onSuccess(action: (T) -> Unit): PekResult<T> = apply {
    if (this is PekResult.Success) action(data)
}

inline fun <T> PekResult<T>.onFailure(action: (DataError) -> Unit): PekResult<T> = apply {
    if (this is PekResult.Failure) action(error)
}

fun <T> PekResult<T>.getOrNull(): T? = (this as? PekResult.Success)?.data

fun <T> PekResult<T>.getOrDefault(fallback: T): T = getOrNull() ?: fallback

val PekResult<*>.isSuccess: Boolean get() = this is PekResult.Success

/**
 * Treats a successful-but-null payload as [DataError.NotFound].
 *
 * Lets a data source express "the call worked, the thing does not exist" by
 * returning null, without needing a non-local return out of a catching block.
 */
fun <T : Any> PekResult<T?>.orNotFound(): PekResult<T> = when (this) {
    is PekResult.Success -> data?.let { PekResult.Success(it) } ?: PekResult.Failure(DataError.NotFound)
    is PekResult.Failure -> this
}
