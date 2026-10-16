package org.setu.studyspotfinder

import android.app.Application
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.data.StudySpotStore

// the Application class is the FIRST thing android creates when the app process starts,
// before MainActivity or any other screen. the manifest points to it with android:name=".StudySpotApp".
// we use it to build the store once, up front, so its ready and shared for the whole app.
class StudySpotApp : Application() {

    // the one store for the whole app, kept here so AppData can grab it
    lateinit var store: StudySpotStore

    override fun onCreate() {
        super.onCreate()
        store = StudySpotStore(this) // created once here at launch
        AppData.init(this)           // tell AppData about it so the screens can use it
    }
}
