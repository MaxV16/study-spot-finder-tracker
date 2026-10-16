package org.setu.studyspotfinder.data

import android.content.Context
import org.setu.studyspotfinder.StudySpotApp

// an object in kotlin is a singleton, there can only ever be one of these
object AppData {

    // this is the store that saves spots to a json file
    // it gets set by init() which is called from the application class
    lateinit var studySpots: StudySpotStore

    // the application calls this once at startup so every screen shares the same store
    fun init(context: Context) {
        // grab the store off our application class (applicationContext is the whole app, not one screen)
        studySpots = (context.applicationContext as StudySpotApp).store
    }
}
