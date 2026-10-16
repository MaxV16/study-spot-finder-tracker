package org.setu.studyspotfinder

import android.app.Application
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.data.StudySpotStore

// this is the application class, the entry point of the whole app
// it runs before any activity so its the right place to set stuff up once
class StudySpotApp : Application() {

    // the store that saves spots to a json file, shared by the whole app
    lateinit var store: StudySpotStore

    // android calls this before any screen is created
    override fun onCreate() {
        super.onCreate()
        // make the one and only store, created once at app launch
        store = StudySpotStore(this)
        // give it to AppData so any screen can grab it later
        AppData.init(this)
    }
}
