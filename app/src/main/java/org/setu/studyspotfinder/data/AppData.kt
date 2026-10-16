package org.setu.studyspotfinder.data

import android.content.Context
import org.setu.studyspotfinder.StudySpotApp

object AppData {

    // this is the store that saves spots to a json file
    // it gets set by init() which is called from the application
    lateinit var studySpots: StudySpotStore

    fun init(context: Context) {
        studySpots = (context.applicationContext as StudySpotApp).store
    }
}