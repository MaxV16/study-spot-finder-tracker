package org.setu.studyspotfinder.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import org.setu.studyspotfinder.data.AuthManager
import org.setu.studyspotfinder.data.JsonStudySpotRepository
import org.setu.studyspotfinder.models.StudySpot
import org.setu.studyspotfinder.ui.auth.AuthActivity
import org.setu.studyspotfinder.ui.main.MainContract
import org.setu.studyspotfinder.ui.main.MainPresenter
import timber.log.Timber
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar


// MainActivity is the home screen aka the launcher, it shows the list of spots, the search box and the menu.
// it only draws stuff now, the searching/sorting/deleting lives in MainPresenter.
class MainActivity : AppCompatActivity(), MainContract.View {

    // refs to the views we make below, kept up here so all the funcs can reach em
    private lateinit var searchBox: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var spotAdapter: StudySpotAdapter
    private lateinit var presenter: MainContract.Presenter

    // onCreate runs once when the screen is first made, its where we build the ui
    override fun onCreate(savedInstanceState: Bundle?) {
        // must be before super.onCreate or the splash gets skipped
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // no session means you dont get in, off to the login screen
        val user = AuthManager.currentUser()
        if (user == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        // pick this users spots file before the presenter reads AppData.studySpots
        AppData.useUser(user.id)

        Timber.i("MainActivity created")

        presenter = MainPresenter(JsonStudySpotRepository(AppData.studySpots))
        createUserInterface()
        presenter.attach(this)
    }

    // onResume runs everytime the screen comes back to the front, like after you save a spot and come back
    override fun onResume() {
        super.onResume()

        // refresh so a new or edited spot shows up when we come back from the form
        if (::presenter.isInitialized) {
            presenter.refresh()
        }
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    // build the whole screen top to bottom in here
    private fun createUserInterface() {

        // grab our 16dp so we arent hardcoding numbers
        val pad = resources.getDimensionPixelSize(R.dimen.screen_padding)

        // a vertical linear layout so everything stacks top to bottom in order
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

        // the Add button, opens the form with no id wich means make a new one
        val addButton = MaterialButton(this).apply {
            text = getString(R.string.add_spot)
            setOnClickListener {
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                startActivity(intent)
            }
        }

        // the search box up top, every keystroke goes to the presenter now
        searchBox = TextInputEditText(this).apply {
            setSingleLine()

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    presenter.onSearchChanged(s?.toString().orEmpty())
                }
            })
        }

        // the material wrapper that gives the box its floating hint
        val searchLayout = TextInputLayout(this).apply {
            hint = getString(R.string.search_hint)
            addView(searchBox)
        }

        // shown instead of the list when there are no spots, hidden for now
        emptyText = TextView(this).apply {
            text = getString(R.string.empty_list)
            textSize = 18f
            setPadding(0, 40, 0, 40)
            visibility = View.GONE
        }

        // one listener instead of two button lambdas. tapping the card opens edit for now,
        // issue 34 changes onSpotClicked to open the detail screen.
        spotAdapter = StudySpotAdapter(
            spots = emptyList(),
            listener = object : StudySpotListener {
                override fun onSpotClicked(spot: StudySpot) = openEditor(spot)
                override fun onEdit(spot: StudySpot) = openEditor(spot)
                override fun onDelete(spot: StudySpot) = deleteWithUndo(spot)
            }
        )

        // the list itself, the layout manager stacks the rows vertically
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = spotAdapter
        }

        // adding all these views one by one by hand was the annoying part, an xml layout woulda been less code
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

        // weight = 1 makes the list fill the rest of the screen and scroll
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
        enableSwipeToDelete()
    }


    // build the little three dot menu in the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // android calls this when you tap a menu item, so we check wich one it was and run the matching code
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // same as the Add button, opens a blank form
            R.id.action_add -> {
                startActivity(Intent(this, AddEditActivity::class.java))
                true
            }
            // the presenter flips the a-z flag and redraws
            R.id.action_sort -> {
                presenter.toggleSort()
                true
            }
            R.id.action_delete_all -> {
                deleteAllSpots()
                true
            }
            // temporary logout spot until the drawer happens on day 8
            R.id.action_logout -> {
                AuthManager.signOut()
                startActivity(
                    Intent(this, AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ask first so you cant nuke everything by accident
    private fun deleteAllSpots() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_all_title)
            .setMessage(R.string.delete_all_message)
            .setPositiveButton(R.string.delete) { _, _ -> presenter.deleteAll() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // opens the form with the spot id so it loads that one
    private fun openEditor(spot: StudySpot) {
        startActivity(
            Intent(this, AddEditActivity::class.java).putExtra("id", spot.id)
        )
    }

    // deletes straight away but gives an undo snackbar so a mistake is fixable.
    // the full spot is kept in the lambda so restore can put the same id back.
    private fun deleteWithUndo(spot: StudySpot) {
        presenter.deleteSpot(spot.id)
        Timber.i("Deleted spot %d", spot.id)

        Snackbar.make(
            recyclerView,
            getString(R.string.spot_deleted, spot.title),
            Snackbar.LENGTH_LONG
        ).setAction(R.string.undo) {
            presenter.restoreSpot(spot)
            Timber.i("Restored spot %d", spot.id)
        }.show()
    }

    // swipe left or right deletes the row and shows the undo snackbar
    private fun enableSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return

                // grab the spot before deleting, the adapter list refreshes underneath us
                val spot = spotAdapter.spotAt(position) ?: return
                deleteWithUndo(spot)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    // called by the presenter when there are spots to show
    override fun showSpots(spots: List<StudySpot>) {
        emptyText.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        spotAdapter.updateSpots(spots)
    }

    // called by the presenter when the list is empty
    override fun showEmpty() {
        spotAdapter.updateSpots(emptyList())
        emptyText.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    // presenter has no context so if it ever needs to say something it hands the string to us
    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}