package org.setu.studyspotfinder.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.setu.studyspotfinder.models.StudySpot
import java.io.File

// this store replaces the placedmarklist that was memory only, this is json stored so ez persistence.
// but instead of keeping the marks in memory it saves them to a json file
// that way they are still there after the app is closed
class StudySpotStore(context: Context) {

    // this is the file we save everything to, its in the apps private storage
    private val file = File(context.filesDir, "spots.json")

    // read the file and turn it back into a list of spots
    fun findAll(): List<StudySpot> {
        // if the file doesnt exist yet just hand back an empty list, no crash
        if (!file.exists()) return emptyList()
        // read the whole file as text then parse it as a json array
        val arr = JSONArray(file.readText())
        val list = ArrayList<StudySpot>()
        // loop over every json object in the array
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            // build a StudySpot out of each json object and add it to the list
            list.add(
                StudySpot(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    desc = obj.getString("desc"),
                    x = obj.getDouble("x"),
                    y = obj.getDouble("y"),
                    // optString so it doesnt blow up on older saves that had no category
                    category = obj.optString("category")
                )
            )
        }
        return list
    }

    // add a new spot, we give it the next free id ourselfs
    fun create(spot: StudySpot) {
        // grab whats allready saved as a mutable list so we can add to it
        val list = findAll().toMutableList()
        // the new id is the biggest id we have plus 1, or 1 if the list was empty
        spot.id = (list.maxOfOrNull { it.id } ?: 0L) + 1
        list.add(spot)
        // write the whole list back to the file
        save(list)
    }

    // update a spot thats allready saved
    fun update(spot: StudySpot): Boolean {
        val list = findAll().toMutableList()
        // find the position of the spot that has the same id
        val index = list.indexOfFirst { it.id == spot.id }
        // if we couldnt find it just return false, nothing to update
        if (index == -1) return false
        // swap the old one out for the new version
        list[index] = spot
        save(list)
        return true
    }

    // removes a spot by its id
    fun delete(id: Long): Boolean {
        val list = findAll().toMutableList()
        // removeAll gives back true only if it actually removed something
        val removed = list.removeAll { it.id == id }
        // only bother saving if something was actually removed
        if (removed) save(list)
        return removed
    }

    // wipes every spot out, used by the delete all menu option
    fun deleteAll() {
        // just save an empty list, thats it
        save(emptyList())
    }

    // find one spot by its id, used when we open the edit screen
    fun findOne(id: Long): StudySpot? {
        // might return null if the id doesnt exist, the ? on the return type handles that
        return findAll().find { it.id == id }
    }

    // turn the whole list into json and write it to the file
    private fun save(list: List<StudySpot>) {
        // this is private so nothing else can write to the file and corrupt it
        val arr = JSONArray()
        for (spot in list) {
            val obj = JSONObject()
            obj.put("id", spot.id)
            obj.put("title", spot.title)
            obj.put("desc", spot.desc)
            obj.put("x", spot.x)
            obj.put("y", spot.y)
            obj.put("category", spot.category)
            arr.put(obj)
        }
        // writeText overwrites the whole file with the new json
        file.writeText(arr.toString())
    }
}
