package az.pekstudios.pekseries.core.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PekResultTest {

    @Test
    fun `map transforms success payload`() {
        val result: PekResult<Int> = PekResult.Success(2)

        assertThat(result.map { it * 3 }).isEqualTo(PekResult.Success(6))
    }

    @Test
    fun `map leaves failure untouched and does not run the transform`() {
        var transformRan = false
        val result: PekResult<Int> = PekResult.Failure(DataError.Network)

        val mapped = result.map { transformRan = true; it * 3 }

        assertThat(mapped).isEqualTo(PekResult.Failure(DataError.Network))
        assertThat(transformRan).isFalse()
    }

    @Test
    fun `getOrDefault returns the fallback only on failure`() {
        assertThat(PekResult.Success(5).getOrDefault(0)).isEqualTo(5)
        assertThat(PekResult.Failure(DataError.Server).getOrDefault(0)).isEqualTo(0)
    }

    @Test
    fun `orNotFound converts a null payload into NotFound`() {
        val result: PekResult<String?> = PekResult.Success(null)

        assertThat(result.orNotFound()).isEqualTo(PekResult.Failure(DataError.NotFound))
    }

    @Test
    fun `orNotFound keeps a present payload`() {
        val result: PekResult<String?> = PekResult.Success("value")

        assertThat(result.orNotFound()).isEqualTo(PekResult.Success("value"))
    }

    @Test
    fun `orNotFound preserves an existing failure rather than masking it as NotFound`() {
        val result: PekResult<String?> = PekResult.Failure(DataError.RateLimited)

        assertThat(result.orNotFound()).isEqualTo(PekResult.Failure(DataError.RateLimited))
    }

    @Test
    fun `onSuccess and onFailure fire only for their own case`() {
        var successes = 0
        var failures = 0

        PekResult.Success(1).onSuccess { successes++ }.onFailure { failures++ }
        PekResult.Failure(DataError.Network).onSuccess { successes++ }.onFailure { failures++ }

        assertThat(successes).isEqualTo(1)
        assertThat(failures).isEqualTo(1)
    }
}
