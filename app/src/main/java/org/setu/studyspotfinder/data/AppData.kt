package org.setu.studyspotfinder.data

import android.content.Context
import org.setu.studyspotfinder.StudySpotApp

// object in kotlin = a singleton, so there is only one AppData for le whole app.
// every screen grabs the same store through here, nobody makes their own one.
object AppData {

    // lateinit = i promise il set this up before anything reads it, read it too early and it can #crash
    lateinit var studySpots: StudySpotStore

    // called once from StudySpotApp.onCreate, grabs the store the app allready made and shares it around
    fun init(context: Context) {
        studySpots = (context.applicationContext as StudySpotApp).store
    }
}
