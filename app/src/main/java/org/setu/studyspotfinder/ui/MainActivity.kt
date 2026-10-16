package org.setu.studyspotfinder.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.data.AppData


// MainActivity is the home screen (the launcher). it shows the list of spots, the search box and the menu.
// we build the whole screen in kotlin here instead of an xml layout file, so you can see it all in one place.
class MainActivity : AppCompatActivity() {

    // these are the references to the views we create below, kept here so other methods can use them
    private lateinit var searchBox: EditText              // the search text field
    private lateinit var recyclerView: RecyclerView      // the scrollable list
    private lateinit var emptyText: TextView             // the "no spots yet" message
    private var sortAZ = false                           // is the list currently sorted a to z?
    private lateinit var spotAdapter: StudySpotAdapter   // draws each row of the list

    // onCreate runs once when the screen is first made. its where we build the ui.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()
    }

    // onResume runs every time the screen comes back to the front (including after you save a spot
    // in AddEditActivity and come back). re-reading the json here is how the list stays up to date.
    override fun onResume() {
        super.onResume()

        // isInitialized check stops a crash on the very first pass when the recyclerView isnt built yet
        if (::recyclerView.isInitialized) {
            displaySpots()
        }
    }

    // this is where we build the screen from scratch, top to bottom
    private fun createUserInterface() {

        // pull our 16dp padding value out of dimens.xml instead of hardcoding numbers
        val pad = resources.getDimensionPixelSize(R.dimen.screen_padding)

        // the root is a vertical LinearLayout, so everything we add stacks top to bottom in order
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        // the top bar
        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.main_title)
            setBackgroundColor(context.getColor(R.color.spot_primary))
            setTitleTextColor(context.getColor(R.color.white))
        }

        // the Add button. tapping it opens the AddEditActivity form with no id, meaning 'make a new spot'
        val addButton = MaterialButton(this).apply {
            text = getString(R.string.add_spot)
            setOnClickListener {
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                startActivity(intent)
            }
        }

        // the search box. the TextWatcher below fires on every single keystroke and re-runs displaySpots()
        searchBox = TextInputEditText(this).apply {
            setSingleLine()

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                // after the text changes, redraw the list so it filters as you type
                override fun afterTextChanged(s: Editable?) {
                    displaySpots()
                }
            })
        }

        // TextInputLayout is the material wrapper that gives the box its floating hint
        val searchLayout = TextInputLayout(this).apply {
            hint = getString(R.string.search_hint)
            addView(searchBox)
        }

        // shown instead of the list when there are no spots (hidden for now)
        emptyText = TextView(this).apply {
            text = getString(R.string.empty_list)
            textSize = 18f
            setPadding(0, 40, 0, 40)
            visibility = View.GONE
        }

        // build the adapter and tell it what to do when a row's Edit or Delete button is tapped.
        // onEdit opens the form WITH the spot id so it loads that spot for editing.
        // onDelete removes the spot from the store then refreshes the list.
        spotAdapter = StudySpotAdapter(
            emptyList(),
            onEdit = { spot ->
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                intent.putExtra("id", spot.id)
                startActivity(intent)
            },
            onDelete = { spot ->
                AppData.studySpots.delete(spot.id)
                displaySpots()
            }
        )

        // the list itself. the adapter draws the rows, the LayoutManager stacks them vertically.
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = spotAdapter
        }

        // now add everything to the root in the order we want them on screen
        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            searchLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            addButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // height 0 + weight 1 means 'take all the space thats left over', so the list fills the screen and scrolls
        root.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(emptyText)

        setContentView(root)
        setSupportActionBar(toolbar)

        // draw the list for the first time
        displaySpots()
    }


    // build the little three dot menu in the toolbar from res/menu/menu_main.xml
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // android calls this when you tap a menu item, so we check which one it was and run the matching code.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // same as the Add button, opens a blank form
            R.id.action_add -> {
                startActivity(Intent(this, AddEditActivity::class.java))
                true
            }
            // flip the a-z flag and redraw. tapping again flips it back
            R.id.action_sort -> {
                sortAZ = !sortAZ
                displaySpots()
                true
            }
            R.id.action_delete_all -> {
                deleteAllSpots()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // asks first so you cant nuke everything by accident, been there done that. rip 1st year assignments :/
    private fun deleteAllSpots() {
        AlertDialog.Builder(this)
            .setTitle("Delete all spots?")
            .setMessage("This removes every study spot and cant be undone.")
            // only if you tap Delete do we actually clear the store
            .setPositiveButton("Delete") { _, _ ->
                AppData.studySpots.deleteAll()
                displaySpots()
            }
            // Cancel does nothing, passing null as the listener just closes the popup
            .setNegativeButton("Cancel", null)
            .show()
    }

    // reads the spots from the store, filters by the search text, optionally sorts, then gives the list to the adapter
    private fun displaySpots() {

        val query = searchBox.text.toString()

        val marks = AppData.studySpots.findAll()
            // keep only spots whose title contains what you typed (ignoring capitals)
            .filter { it.title.contains(query, ignoreCase = true) }
            // if sortAZ is on, sort them by title, otherwise leave them as they are
            .let { if (sortAZ) it.sortedBy { spot -> spot.title.lowercase() } else it }

        // if there is nothing to show, show the message and hide the list, and the other way round
        emptyText.visibility = if (marks.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (marks.isEmpty()) View.GONE else View.VISIBLE

        // hand the final list to the adapter so it redraws the rows
        spotAdapter.updateSpots(marks)
    }
}
