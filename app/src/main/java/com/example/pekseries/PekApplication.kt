package com.example.pekseries

import android.app.Application
import com.example.pekseries.data.NetworkClient
import timber.log.Timber

class PekApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        NetworkClient.init(this)
    }
}