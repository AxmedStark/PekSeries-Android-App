package az.pekstudios.pekseries.core.work

/**
 * Deep-link contract shared between the notification builder and the app's
 * navigation graph.
 *
 * :core:work cannot reference MainActivity without inverting the module
 * dependency, so the notification fires an implicit VIEW intent at this URI and
 * the app module declares the matching intent-filter.
 */
object PekDeepLink {
    const val SCHEME = "pekseries"
    const val HOST_SHOW = "show"

    /** pekseries://show/{showId} */
    fun showUri(showId: String): String = "$SCHEME://$HOST_SHOW/$showId"

    const val SHOW_URI_PATTERN = "$SCHEME://$HOST_SHOW/{showId}"
}

object PekNotificationChannels {
    const val NEW_EPISODES_ID = "pekseries_new_episodes"
}
