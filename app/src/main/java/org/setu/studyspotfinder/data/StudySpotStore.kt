package org.setu.studyspotfinder.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.setu.studyspotfinder.models.StudySpot
import timber.log.Timber
import java.io.File

// the old version only kept spots in an ArrayList in memory so they vanished when the app closed.
// now we write them to a json file on the phone so they are still there next time you open the app.
// the activities never touch the file directly, they only talk to this class, thats the whole point.
class StudySpotStore(  // this is the persistence layer of le app
    context: Context,
    userId: String = "guest"
) {
    // one file per account, the user id is a uuid so its safe as a filename
    private val file = File(context.filesDir, "spots_$userId.json")

    //opens the file, turn the json back into a list of StudySpot objects. gets called everytime we need da list, file is always the source of truth, fileops :D
    fun findAll(): List<StudySpot> {
        if (!file.exists()) return emptyList() //if da file doesnt not exist, return an empty list instead of #crashing.
        val arr = JSONArray(file.readText()) //grabs the whole file as a string, then json array parses it into a list of json objects.
        val list = ArrayList<StudySpot>()
        for (i in 0 until arr.length()) { //loops over every json entry
            val obj = arr.getJSONObject(i) //then rebuilds a studyspot object out of em
            list.add(
                StudySpot(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    desc = obj.getString("desc"),
                    x = obj.getDouble("x"),
                    y = obj.getDouble("y"),
                    category = obj.optString("category"), //using optstring just incase old spots dont have a cat yet, so we dont crash.
                    // optString turns a json null into the text "null", and older versions saved that
                    // text back into the file, so both have to count as "no photo"
                    imageUri = if (obj.isNull("imageUri")) null
                    else obj.optString("imageUri").ifBlank { null }?.takeIf { it != "null" }
                )
            )
        }
        return list
    }

    fun create(spot: StudySpot) {
        spot.validate() // no blank names or crazy coordinates get saved
        val list = findAll().toMutableList()  //turns the list into a mutable list so we can add to it
        spot.id = (list.maxOfOrNull { it.id } ?: 0L) + 1 //takes the biggest id we have then adds +1, epic new id for spot, the weird ?: 0l means if the list empty then be 0 instead of crashing
        list.add(spot)
        save(list)
    }

    fun update(spot: StudySpot): Boolean { //replaces an already saved spot
        spot.validate()
        val list = findAll().toMutableList() //turns the list into a mutable list so we can add to it
        val index = list.indexOfFirst { it.id == spot.id } //indexoffirst gives us the position in the list, or -1 if there isnt one
        if (index == -1) return false //if theres no spot with that id, return false
        list[index] = spot //else replace it with the new spot
        save(list)
        return true
    }

    fun delete(id: Long): Boolean { //deletes a spot by id
        val list = findAll().toMutableList()
        val removed = list.removeAll { it.id == id }
        // only bother writing the file if something really got removed
        if (removed) save(list)
        return removed
    }

    // puts a deleted spot back exactly as it was, same id and all.
// only if its not already there, that would make a duplicate.
    fun restore(spot: StudySpot) {
        val list = findAll().toMutableList()
        if (list.none { it.id == spot.id }) {
            list.add(spot)
            save(list)
        }
    }

    fun deleteAll() { //instead of removing one by one, we just overwrite the file with an empty one #efficient. used in the 3 dots button.
        save(emptyList()) //called by xml
    }
    fun findOne(id: Long): StudySpot? { //this func is used for when you tap edit, the form can be filled in with the spots current vals.
        return findAll().find { it.id == id }
    }
    private fun save(list: List<StudySpot>) { //tursn the whole list into json and overwrites file. private so no other class can use it.
        val arr = JSONArray()  //creates a json array
        for (spot in list) { //loops over every spot, turns it into a json object and adds it to the array
            val obj = JSONObject() //creates a json object which in turn each value gets added to
            obj.put("id", spot.id)
            obj.put("title", spot.title)
            obj.put("desc", spot.desc)
            obj.put("x", spot.x)
            obj.put("y", spot.y)
            obj.put("category", spot.category)
            obj.put("imageUri", spot.imageUri ?: JSONObject.NULL)
            arr.put(obj)
        }
        // toString() turns the array into text and writeText throws it onto disk
        file.writeText(arr.toString())
    }

    companion object {
        private const val LEGACY_FILE = "spots.json"
        private const val MIGRATED_FILE = "spots_migrated.bak"

        // assignment 1 wrote everything to spots.json. copy it into the first account that logs in,
        // then rename it so the next new account doesnt get the same list again.
        fun migrateLegacy(context: Context, userId: String) {
            val legacy = File(context.filesDir, LEGACY_FILE)
            val target = File(context.filesDir, "spots_$userId.json")

            if (legacy.exists() && !target.exists()) {
                legacy.copyTo(target, overwrite = false)
                legacy.renameTo(File(context.filesDir, MIGRATED_FILE))
                Timber.i("Migrated legacy spots.json to spots_%s.json", userId)
            }
        }
    }
}
