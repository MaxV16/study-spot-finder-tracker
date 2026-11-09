package org.setu.studyspotfinder.data

import android.content.Context
import timber.log.Timber

// object in kotlin = a singleton, so there is only one AppData for le whole app.
// every screen grabs the same store through here, nobody makes their own one.
object AppData {

    private lateinit var appContext: Context

    // lateinit = i promise il set this up before anything reads it, read it too early and it can #crash
    // private set because the store only changes through useUser, not from a screen
    lateinit var studySpots: StudySpotStore
        private set

    // called once from StudySpotApp.onCreate, just keeps the context around for later
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // call once after login, before any presenter or screen reads spots
    fun useUser(userId: String) {
        StudySpotStore.migrateLegacy(appContext, userId)
        studySpots = StudySpotStore(appContext, userId)
        Timber.i("Active spot store for user %s", userId)
    }
}
