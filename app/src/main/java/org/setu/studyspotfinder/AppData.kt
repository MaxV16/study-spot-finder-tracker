package org.setu.studyspotfinder

import android.content.Context

object AppData {

    // this is the store that saves spots to a json file
    // it gets set by init() which is called from the application
    lateinit var studySpots: StudySpotStore

    fun init(context: Context) {
        studySpots = (context.applicationContext as StudySpotApp).store
    }
}
          