package az.pekstudios.pekseries.core.data

import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ErrorMappingTest {

    private fun httpError(code: Int) = HttpException(
        Response.error<Any>(code, "".toResponseBody("text/plain".toMediaType())),
    )

    @Test
    fun `http status codes map to their specific errors`() {
        assertThat(httpError(401).toDataError()).isEqualTo(DataError.Unauthenticated)
        assertThat(httpError(403).toDataError()).isEqualTo(DataError.Unauthenticated)
        assertThat(httpError(404).toDataError()).isEqualTo(DataError.NotFound)
        assertThat(httpError(500).toDataError()).isEqualTo(DataError.Server)
        assertThat(httpError(503).toDataError()).isEqualTo(DataError.Server)
    }

    /** TVMaze rate-limits aggressively; this is the case worth naming. */
    @Test
    fun `429 maps to RateLimited rather than a generic failure`() {
        assertThat(httpError(429).toDataError()).isEqualTo(DataError.RateLimited)
    }

    @Test
    fun `io failures map to Network`() {
        assertThat(IOException("offline").toDataError()).isEqualTo(DataError.Network)
        assertThat(SocketTimeoutException().toDataError()).isEqualTo(DataError.Network)
    }

    @Test
    fun `a missing signed-in user maps to Unauthenticated`() {
        assertThat(NotSignedInException().toDataError()).isEqualTo(DataError.Unauthenticated)
    }

    @Test
    fun `anything unrecognised keeps the cause for debugging`() {
        val cause = IllegalStateException("boom")

        assertThat(cause.toDataError()).isEqualTo(DataError.Unknown(cause))
    }

    @Test
    fun `runCatchingData wraps a thrown error as a typed failure`() = runTest {
        val result = runCatchingData<Int>("test") { throw httpError(429) }

        assertThat(result).isEqualTo(PekResult.Failure(DataError.RateLimited))
    }

    @Test
    fun `runCatchingData returns the value on success`() = runTest {
        assertThat(runCatchingData("test") { 42 }).isEqualTo(PekResult.Success(42))
    }

    /**
     * Swallowing CancellationException breaks structured concurrency: a screen
     * that goes away would leave its coroutines running. The old repository
     * caught bare Exception and had exactly this bug.
     */
    @Test(expected = CancellationException::class)
    fun `runCatchingData rethrows cancellation instead of converting it`() = runTest {
        runCatchingData<Int>("test") { throw CancellationException("cancelled") }
    }
}
