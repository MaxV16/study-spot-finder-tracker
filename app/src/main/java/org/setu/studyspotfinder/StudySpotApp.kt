package org.setu.studyspotfinder

import android.app.Application
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.data.StudySpotStore

// this is the application class
// it runs before any activity and sets up the store
class StudySpotApp : Application() {

    // the store that saves spots to a json file
    lateinit var store: StudySpotStore

    override fun onCreate() {
        super.onCreate()
        store = StudySpotStore(this) //created once at app launch
        AppData.init(this)
    }
}
                     