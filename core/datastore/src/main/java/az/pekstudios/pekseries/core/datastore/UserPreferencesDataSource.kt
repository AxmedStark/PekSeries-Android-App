package az.pekstudios.pekseries.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("display_name_override")
        val PHOTO_URI = stringPreferencesKey("photo_uri_override")
        val PUSH_ENABLED = booleanPreferencesKey("push_enabled")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { throwable ->
            // A corrupt preferences file should degrade to defaults, not crash
            // the app on launch. Anything that is not an IOException is a real
            // programming error and is left to propagate.
            if (throwable is IOException) {
                Timber.e(throwable, "Failed to read user preferences; using defaults")
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { prefs ->
            UserPreferences(
                displayNameOverride = prefs[Keys.DISPLAY_NAME]?.takeIf(String::isNotBlank),
                photoUriOverride = prefs[Keys.PHOTO_URI]?.takeIf(String::isNotBlank),
                pushEnabled = prefs[Keys.PUSH_ENABLED] ?: true,
            )
        }

    /** Passing null clears the override and restores the auth provider's value. */
    suspend fun setDisplayNameOverride(name: String?) = dataStore.edit { prefs ->
        if (name.isNullOrBlank()) prefs.remove(Keys.DISPLAY_NAME) else prefs[Keys.DISPLAY_NAME] = name
    }

    suspend fun setPhotoUriOverride(uri: String?) = dataStore.edit { prefs ->
        if (uri.isNullOrBlank()) prefs.remove(Keys.PHOTO_URI) else prefs[Keys.PHOTO_URI] = uri
    }

    suspend fun setPushEnabled(enabled: Boolean) = dataStore.edit { prefs ->
        prefs[Keys.PUSH_ENABLED] = enabled
    }

    /** Called on logout so the next account does not inherit these overrides. */
    suspend fun clear() = dataStore.edit { prefs -> prefs.clear() }
}
