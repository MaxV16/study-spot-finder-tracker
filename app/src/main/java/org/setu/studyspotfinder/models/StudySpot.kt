package org.setu.studyspotfinder.models

data class StudySpot( // this class is one study spot, so every spot/place the user saves becomes one of these objects.
    //all vars because we want them to be changed later. if vals then read only :(
    var id: Long = 0L, //starts at 0 and the studyspotstore class gives it a real id when we save it. (studyspotstore.create)
    var title: String = "",
    var desc: String = "",
    var x: Double = 0.0, //we shall use these later for maps api!!
    var y: Double = 0.0,
    var category: String = "",
    var imageUri: String? = null // null means no photo, also covers old json files that dont have this yet
) {
    // only called when saving, not when the empty form opens
    fun validate() {
        require(title.isNotBlank()) { "Name must not be empty." }
        require(x.isFinite() && x in -90.0..90.0) { "Latitude must be between -90 and 90." }
        require(y.isFinite() && y in -180.0..180.0) { "Longitude must be between -180 and 180." }
    }
}
