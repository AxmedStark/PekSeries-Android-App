package az.pekstudios.pekseries.core.datastore

/**
 * Locally stored user preferences.
 *
 * [displayNameOverride] and [photoUriOverride] are null until the user edits
 * their profile. Null means "fall back to whatever the auth provider gave us",
 * which is what keeps a Google display name working while still letting the
 * user replace it.
 */
data class UserPreferences(
    val displayNameOverride: String? = null,
    val photoUriOverride: String? = null,
    val pushEnabled: Boolean = true,
)
