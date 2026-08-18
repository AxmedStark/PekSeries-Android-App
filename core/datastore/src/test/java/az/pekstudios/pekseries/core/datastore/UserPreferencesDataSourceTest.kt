package az.pekstudios.pekseries.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesDataSourceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // backgroundScope, not the TestScope itself: DataStore keeps a long-lived
    // collector alive, and runTest would wait for it to finish forever.
    private fun dataSource(scope: CoroutineScope) = UserPreferencesDataSource(
        PreferenceDataStoreFactory.create(scope = scope) {
            tempFolder.newFile("user_prefs_${System.nanoTime()}.preferences_pb")
        },
    )

    @Test
    fun `defaults to no overrides and push enabled`() = runTest(UnconfinedTestDispatcher()) {
        val prefs = dataSource(backgroundScope).userPreferences.first()

        assertThat(prefs.displayNameOverride).isNull()
        assertThat(prefs.photoUriOverride).isNull()
        assertThat(prefs.pushEnabled).isTrue()
    }

    @Test
    fun `a stored display name is read back`() = runTest(UnconfinedTestDispatcher()) {
        val source = dataSource(backgroundScope)

        source.setDisplayNameOverride("Ahmed")

        assertThat(source.userPreferences.first().displayNameOverride).isEqualTo("Ahmed")
    }

    /**
     * Null means "fall back to the auth provider", which is what makes a Google
     * display name reappear after the user clears their custom one.
     */
    @Test
    fun `null clears the display name override`() = runTest(UnconfinedTestDispatcher()) {
        val source = dataSource(backgroundScope)
        source.setDisplayNameOverride("Ahmed")

        source.setDisplayNameOverride(null)

        assertThat(source.userPreferences.first().displayNameOverride).isNull()
    }

    @Test
    fun `a blank name is treated as cleared, not stored as an empty string`() =
        runTest(UnconfinedTestDispatcher()) {
            val source = dataSource(backgroundScope)

            source.setDisplayNameOverride("   ")

            assertThat(source.userPreferences.first().displayNameOverride).isNull()
        }

    @Test
    fun `the push flag round-trips`() = runTest(UnconfinedTestDispatcher()) {
        val source = dataSource(backgroundScope)

        source.setPushEnabled(false)

        assertThat(source.userPreferences.first().pushEnabled).isFalse()
    }

    @Test
    fun `clear removes every override and restores defaults`() = runTest(UnconfinedTestDispatcher()) {
        val source = dataSource(backgroundScope)
        source.setDisplayNameOverride("Ahmed")
        source.setPhotoUriOverride("content://photo/1")
        source.setPushEnabled(false)

        source.clear()

        val prefs = source.userPreferences.first()
        assertThat(prefs.displayNameOverride).isNull()
        assertThat(prefs.photoUriOverride).isNull()
        assertThat(prefs.pushEnabled).isTrue()
    }

    @Test
    fun `observers see a change as it is written`() = runTest(UnconfinedTestDispatcher()) {
        val source = dataSource(backgroundScope)

        source.userPreferences.test {
            assertThat(awaitItem().displayNameOverride).isNull()

            source.setDisplayNameOverride("Ahmed")

            assertThat(awaitItem().displayNameOverride).isEqualTo("Ahmed")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
