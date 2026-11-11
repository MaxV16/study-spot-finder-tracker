package org.setu.studyspotfinder

import android.app.Application
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.data.AuthManager
import timber.log.Timber

// the application class is the very first thing android makes when the app starts, before any screen.
// the manifest points at it with android:name=".StudySpotApp".
// no store here anymore, the store depends on who is logged in so AppData makes it after login.
class StudySpotApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // only log in debug builds, release stays quiet
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        AuthManager.init(this)
        AppData.init(this) // just saves the context, the spot store comes later in useUser

        Timber.i("StudySpotApp started")
    }
}
