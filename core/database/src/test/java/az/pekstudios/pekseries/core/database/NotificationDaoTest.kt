package az.pekstudios.pekseries.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pinned to SDK 35: Robolectric needs Java 21 to emulate SDK 36, and the project
 * toolchain is Java 17. Room's behaviour here does not vary by SDK level.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NotificationDaoTest {

    private lateinit var database: PekDatabase
    private lateinit var dao: NotificationDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PekDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.notificationDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `inserted notifications are observed newest first`() = runTest {
        dao.insertNotification(entity(title = "older", timestamp = 1_000))
        dao.insertNotification(entity(title = "newer", timestamp = 2_000))

        dao.getAllNotifications().test {
            val titles = awaitItem().map { it.title }
            assertThat(titles).containsExactly("newer", "older").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the flow emits again when a notification arrives`() = runTest {
        dao.getAllNotifications().test {
            assertThat(awaitItem()).isEmpty()

            dao.insertNotification(entity(title = "incoming"))

            assertThat(awaitItem().single().title).isEqualTo("incoming")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing removes every row`() = runTest {
        dao.insertNotification(entity(title = "a"))
        dao.insertNotification(entity(title = "b"))

        val deleted = dao.deleteAllNotifications()

        assertThat(deleted).isEqualTo(2)
        dao.getAllNotifications().test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** showId is what the deep link needs, so it must survive the round trip. */
    @Test
    fun `showId is persisted and may be absent`() = runTest {
        dao.insertNotification(entity(title = "with", showId = "42"))
        dao.insertNotification(entity(title = "without", showId = null))

        dao.getAllNotifications().test {
            val byTitle = awaitItem().associateBy { it.title }
            assertThat(byTitle.getValue("with").showId).isEqualTo("42")
            assertThat(byTitle.getValue("without").showId).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun entity(
        title: String,
        showId: String? = null,
        timestamp: Long = System.currentTimeMillis(),
    ) = NotificationEntity(
        title = title,
        body = "body",
        showId = showId,
        timestamp = timestamp,
    )
}
