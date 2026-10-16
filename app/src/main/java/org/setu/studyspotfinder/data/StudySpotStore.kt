package org.setu.studyspotfinder.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.setu.studyspotfinder.models.StudySpot
import java.io.File

// THIS is the persistence layer, aka the filing cabinet of the app.
// the old version only kept spots in an ArrayList in memory so they vanished when the app closed.
// now we write them to a json file on the phone so they are still there next time you open the app.
// the activities never touch the file directly, they only talk to this class, thats the whole point.
class StudySpotStore(context: Context) {

    // the file we read from and write to. filesDir is the apps own private storage,
    // so only our app can see it and it gets wiped if you uninstall.
    // the Context is only needed to find filesDir, thats why this class asks for one.
    private val file = File(context.filesDir, "spots.json")

    // READ: open the file, turn the json back into a list of StudySpot objects.
    // this gets called every time we need the list, so the file is always the source of truth.
    fun findAll(): List<StudySpot> {
        // first run of the app there is no file yet, so just return nothing instead of crashing
        if (!file.exists()) return emptyList()
        // readText() grabs the whole file as a string, JSONArray parses it into a list of json objects
        val arr = JSONArray(file.readText())
        val list = ArrayList<StudySpot>()
        // loop over every json entry and rebuild a proper StudySpot out of it
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                StudySpot(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    desc = obj.getString("desc"),
                    x = obj.getDouble("x"),
                    y = obj.getDouble("y"),
                    // optString instead of getString because old saved spots might not have a category yet,
                    // optString gives us "" instead of blowing up
                    category = obj.optString("category")
                )
            )
        }
        return list
    }

    // CREATE: add a brand new spot.
    // we work out the next free id ourselfs so no two spots ever share one.
    fun create(spot: StudySpot) {
        val list = findAll().toMutableList()
        // take the biggest id we currently have, add 1, thats the new spots id.
        // the ?: 0L part means 'if the list is empty use 0' so the very first spot gets id 1
        spot.id = (list.maxOfOrNull { it.id } ?: 0L) + 1
        list.add(spot)
        save(list)
    }

    // UPDATE: replace an already saved spot.
    // we look for the spot with the same id and swap it for the new values.
    fun update(spot: StudySpot): Boolean {
        val list = findAll().toMutableList()
        // indexOfFirst gives us the position in the list, or -1 if there isnt one
        val index = list.indexOfFirst { it.id == spot.id }
        if (index == -1) return false
        list[index] = spot
        save(list)
        return true
    }

    // DELETE: remove one spot by its id and tell the caller if it actually removed something
    fun delete(id: Long): Boolean {
        val list = findAll().toMutableList()
        val removed = list.removeAll { it.id == id }
        // only bother writing the file if something really got removed
        if (removed) save(list)
        return removed
    }

    // DELETE EVERYTHING: saving an empty list basically clears the file, used by the 'Delete all' menu option
    fun deleteAll() {
        save(emptyList())
    }

    // FIND ONE: used when you tap Edit so the form can be filled in with the spots current values
    fun findOne(id: Long): StudySpot? {
        return findAll().find { it.id == id }
    }

    // WRITE: turn the whole list into json and overwrite the file.
    // notice this is private, so the outside world cant write to the file behind our back,
    // everything has to go through create/update/delete above.
    // the pattern everywhere in this class is: read the list -> change it -> write it all back.
    private fun save(list: List<StudySpot>) {
        val arr = JSONArray()
        for (spot in list) {
            // each spot becomes a little json object with the same field names we read back in findAll()
            val obj = JSONObject()
            obj.put("id", spot.id)
            obj.put("title", spot.title)
            obj.put("desc", spot.desc)
            obj.put("x", spot.x)
            obj.put("y", spot.y)
            obj.put("category", spot.category)
            arr.put(obj)
        }
        // toString() turns the array into text and writeText throws it onto disk
        file.writeText(arr.toString())
    }
}
