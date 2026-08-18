package az.pekstudios.pekseries

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import az.pekstudios.pekseries.core.ui.theme.PekSeriesTheme
import az.pekstudios.pekseries.core.work.PekDeepLink
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pendingShowId = MutableStateFlow<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingShowId.value = intent.extractShowId()

        requestNotificationPermissionIfNeeded()
        logFcmTokenInDebug()

        enableEdgeToEdge()
        setContent {
            PekSeriesTheme {
                PekSeriesApp(
                    pendingShowId = pendingShowId.asStateFlow(),
                    onShowIdHandled = { pendingShowId.value = null },
                )
            }
        }
    }

    /**
     * With launchMode=singleTop a notification tap while the app is already
     * running arrives here rather than in onCreate. Without this override, that
     * tap did nothing.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.extractShowId()?.let { pendingShowId.value = it }
    }

    /**
     * Accepts both the deep-link URI used by the notification's contentIntent
     * and the plain "showId" extra that FCM attaches when it auto-displays a
     * notification-payload message.
     */
    private fun Intent.extractShowId(): String? {
        val fromUri = data
            ?.takeIf { it.scheme == PekDeepLink.SCHEME && it.host == PekDeepLink.HOST_SHOW }
            ?.lastPathSegment

        return fromUri?.takeIf { it.isNotBlank() }
            ?: getStringExtra(EXTRA_SHOW_ID)?.takeIf { it.isNotBlank() }
    }

    /** Prints the token so a test push can be sent from the Firebase console. */
    private fun logFcmTokenInDebug() {
        if (!BuildConfig.DEBUG) return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.tag("FCM_TOKEN").d(task.result)
            } else {
                Timber.tag("FCM_TOKEN").w(task.exception, "Could not fetch FCM token")
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private companion object {
        const val EXTRA_SHOW_ID = "showId"
    }
}
