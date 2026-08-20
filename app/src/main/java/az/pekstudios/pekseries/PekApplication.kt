package az.pekstudios.pekseries

import android.app.Application
import az.pekstudios.pekseries.core.domain.repository.TopicSynchronizer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class PekApplication : Application() {

    @Inject
    lateinit var topicSynchronizer: TopicSynchronizer

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // FCM topic subscriptions are bound to the registration token, so a
        // reinstall or token rotation orphans them all. Reconciling on every
        // start (not only at sign-in) is what makes push delivery self-healing.
        topicSynchronizer.start()
    }
}
