package org.setu.studyspotfinder

import android.app.Application
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.data.StudySpotStore

// the application class is the very first thing android makes when the app starts, before any screen.
// the manifest points at it with android:name=".StudySpotApp".
// we build the one store in here so its ready and shared for the whole app :D
class StudySpotApp : Application() {

    // the one and only store, kept here so AppData can grab it
    lateinit var store: StudySpotStore

    override fun onCreate() {
        super.onCreate()
        store = StudySpotStore(this) // made once at launch, not once per screen
        AppData.init(this)    // tells AppData about it so the screens can use it
    }
}
