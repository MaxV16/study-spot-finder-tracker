package org.setu.studyspotfinder.data

import android.content.Context
import org.setu.studyspotfinder.StudySpotApp

// 'object' in kotlin means this is a single shared instance (a singleton),
// so the whole app has exactly ONE AppData and every screen can reach the same store through it.
// the screens never make their own StudySpotStore, they just ask AppData for the one we made at startup.
object AppData {

    // lateinit = 'i promise to set this up later, before anything reads it'.
    // reading it too early would crash, but StudySpotApp.onCreate runs before any screen so we are safe.
    lateinit var studySpots: StudySpotStore

    // called once from StudySpotApp.onCreate. we cast the application context back to our own
    // StudySpotApp so we can grab the store it already built, then hand it to everyone via the variable above.
    fun init(context: Context) {
        studySpots = (context.applicationContext as StudySpotApp).store
    }
}
