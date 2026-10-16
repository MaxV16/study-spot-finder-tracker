package org.setu.studyspotfinder.models

data class StudySpot( // this class is one study spot, so every spot/place the user saves becomes one of these objects.
    //all vars because we want them to be changed later. if vals then read only :(
    var id: Long = 0L, //starts at 0 and the studyspotstore class gives it a real id when we save it. (studyspotstore.create)
    var title: String = "",
    var desc: String = "",
    var x: Double = 0.0, //we shall use these later for maps api!!
    var y: Double = 0.0,
    var category: String = ""
)
