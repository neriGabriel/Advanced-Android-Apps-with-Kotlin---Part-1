package com.udacity

import android.app.Application
import timber.log.Timber

class LoadingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BuildConfig.DEBUG.takeIf{it}.let {Timber.plant(Timber.DebugTree()) }
    }
}