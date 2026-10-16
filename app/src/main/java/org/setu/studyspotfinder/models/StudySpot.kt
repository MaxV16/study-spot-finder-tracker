package org.setu.studyspotfinder.models

data class StudySpot(
    var id: Long = 0L,
    var title: String = "",
    var desc: String = "",
    var x: Double = 0.0,
    var y: Double = 0.0,
    var category: String = ""
)