package az.pekstudios.pekseries.core.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import az.pekstudios.pekseries.core.database.NotificationDao
import az.pekstudios.pekseries.core.database.NotificationEntity
import az.pekstudios.pekseries.core.domain.di.ApplicationScope
import az.pekstudios.pekseries.core.domain.repository.TopicSynchronizer
import az.pekstudios.pekseries.core.ui.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class PekFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationDao: NotificationDao

    @Inject
    lateinit var topicSynchronizer: TopicSynchronizer

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: getString(R.string.notification_default_title)
        val body = message.data["body"] ?: getString(R.string.notification_default_body)
        val showId = message.data["showId"]

        // onMessageReceived already runs off the main thread, and the process may
        // be torn down as soon as it returns. Blocking here guarantees the write
        // lands; the previous CoroutineScope(Dispatchers.IO) was unscoped and
        // could be killed mid-insert.
        runBlocking {
            runCatching {
                notificationDao.insertNotification(
                    NotificationEntity(title = title, body = body, showId = showId),
                )
            }.onFailure { Timber.e(it, "Failed to persist notification") }
        }

        showNotification(title, body, showId)
    }

    /**
     * A new registration token starts with no topic subscriptions, and FCM does
     * not carry the old ones over, so every topic has to be re-applied.
     *
     * This previously only logged, which is why a reinstall silently ended all
     * push delivery until the user happened to sign in again.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.i("FCM token rotated; re-subscribing topics")

        // Application-scoped: reconciliation outlives this callback.
        applicationScope.launch { topicSynchronizer.reconcile() }
    }

    private fun showNotification(title: String, message: String, showId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                PekNotificationChannels.NEW_EPISODES_ID,
                getString(R.string.notification_channel_new_episodes),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )

        val notification = NotificationCompat.Builder(this, PekNotificationChannels.NEW_EPISODES_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply { showId?.let { setContentIntent(deepLinkIntent(it)) } }
            .build()

        // Keyed on the show so a second episode notification replaces the first
        // rather than stacking up an unbounded list of entries.
        val notificationId = showId?.hashCode() ?: Random.nextInt()
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Implicit VIEW intent at pekseries://show/{id}, restricted to this package.
     * Without a contentIntent — which is what was missing — tapping the
     * notification did nothing at all.
     */
    private fun deepLinkIntent(showId: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, PekDeepLink.showUri(showId).toUri()).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return PendingIntent.getActivity(
            this,
            showId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
