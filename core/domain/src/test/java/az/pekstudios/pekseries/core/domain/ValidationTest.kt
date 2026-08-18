package az.pekstudios.pekseries.core.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ValidationTest {

    @Test
    fun `accepts ordinary addresses`() {
        listOf(
            "a@b.co",
            "torpeda250@gmail.com",
            "first.last+tag@sub.example.org",
        ).forEach { assertThat(Validation.isValidEmail(it)).isTrue() }
    }

    @Test
    fun `rejects malformed addresses`() {
        listOf(
            "",
            "  ",
            "no-at-sign",
            "@nolocal.com",
            "no@domain",
            "no@@double.com",
            "spaces in@example.com",
        ).forEach { assertThat(Validation.isValidEmail(it)).isFalse() }
    }

    @Test
    fun `trims surrounding whitespace before validating`() {
        assertThat(Validation.isValidEmail("  user@example.com  ")).isTrue()
    }

    @Test
    fun `password must reach the minimum length`() {
        assertThat(Validation.isValidPassword("a".repeat(Validation.MIN_PASSWORD_LENGTH))).isTrue()
        assertThat(Validation.isValidPassword("a".repeat(Validation.MIN_PASSWORD_LENGTH - 1))).isFalse()
        assertThat(Validation.isValidPassword("")).isFalse()
    }
}
