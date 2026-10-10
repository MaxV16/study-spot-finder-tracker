package org.setu.studyspotfinder

import android.app.Application

// this is the application class
// it runs before any activity and sets up the store
class StudySpotApp : Application() {

    // the store that saves spots to a json file
    lateinit var store: StudySpotStore

    override fun onCreate() {
        super.onCreate()
        store = StudySpotStore(this)
        AppData.init(this)
    }
}
                     