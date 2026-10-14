package org.setu.studyspotfinder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// this store replaces the placedmarklist that was memory only, this is json stored so ez persistence.
// but instead of keeping the marks in memory it saves them to a json file
// that way they are still there after the app is closed
class StudySpotStore(context: Context) {

    // this is the file we save everything to, its in the apps private storage
    private val file = java.io.File(context.filesDir, "spots.json")

    // read the file and turn it back into a list of spots
    fun findAll(): List<StudySpot> {
        if (!file.exists()) return emptyList()
        val arr = JSONArray(file.readText())
        val list = ArrayList<StudySpot>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                StudySpot(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    desc = obj.getString("desc"),
                    x = obj.getDouble("x"),
                    y = obj.getDouble("y"),
                    category = obj.optString("category")
                )
            )
        }
        return list
    }

    // add a new spots, we give it the next free id ourselfs
    fun create(spot: StudySpot) {
        val list = findAll().toMutableList()
        spot.id = (list.maxOfOrNull { it.id } ?: 0L) + 1
        list.add(spot)
        save(list)
    }

    // update a spot thats allready saved
    fun update(spot: StudySpot): Boolean {
        val list = findAll().toMutableList()
        val index = list.indexOfFirst { it.id == spot.id }
        if (index == -1) return false
        list[index] = spot
        save(list)
        return true
    }

    // removes a spot by its id
    fun delete(id: Long): Boolean {
        val list = findAll().toMutableList()
        val removed = list.removeAll { it.id == id }
        if (removed) save(list)
        return removed
    }

    // wipes every spot out, used by the delete all menu option
    fun deleteAll() {
        save(emptyList())
    }

    // find one spot by its id
    fun findOne(id: Long): StudySpot? {
        return findAll().find { it.id == id }
    }

    // turn the whole list into json and write it to the file
    private fun save(list: List<StudySpot>) {
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
        file.writeText(arr.toString())
    }
}