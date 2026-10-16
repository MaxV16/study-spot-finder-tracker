package org.setu.studyspotfinder.models

// data class = kotlin gives us equals(), hashCode(), toString() and copy() for free.
// this class is ONE study spot, so every place the user saves becomes one of these objects.
// think of it like a blank form, each field below is one line on the form.
// 'var' means the value can change later, if it was 'val' it would be read only.
data class StudySpot(
    // the id is how we tell spots apart, the user never types this.
    // it starts at 0 and the store gives it a real unique number when we save it (see StudySpotStore.create).
    // Long is just a big whole number, we use it so ids never realistically run out.
    var id: Long = 0L,
    // the name the user gives the spot, like '3rd floor library'
    var title: String = "",
    // a short note about the spot, like 'quiet and has plugs'
    var desc: String = "",
    // x and y are the co ordinates so we can plot the spot on a map later.
    // Double lets us use decimals, right now the user just types them in the form.
    var x: Double = 0.0,
    var y: Double = 0.0,
    // what kind of spot it is (Library, Cafe, etc). the value comes from the dropdown in AddEditActivity.
    var category: String = ""
)
