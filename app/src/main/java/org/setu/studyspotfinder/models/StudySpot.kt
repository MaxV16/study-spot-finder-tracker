package org.setu.studyspotfinder.models

// this is the model, its just a plain data class that holds one study spot
// data class means kotlin makes equals/hashCode/toString/copy for us wich saves alot of typing
data class StudySpot(
    // the id is handed out by the store when we save, before that it just stays 0
    var id: Long = 0L,
    // the name of the spot, this is the main thing we show in the list and search on
    var title: String = "",
    // a short descripion of the spot
    var desc: String = "",
    // x and y are the co ordinates of the spot (basically where it sits on the map)
    var x: Double = 0.0,
    var y: Double = 0.0,
    // the type of spot, comes from the dropdown (Library/Cafe etc)
    var category: String = ""
)
