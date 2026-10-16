package org.setu.studyspotfinder.ui


import android.widget.ArrayAdapter
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.models.StudySpot

// this screen does both jobs, adding a new spot and editing an existing one
class AddEditActivity : AppCompatActivity() {

    // the actual inputs the user types into
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText

    // the material wrappers around the inputs so we get floating hints + error messages
    private lateinit var titleLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout

    private lateinit var xLayout: TextInputLayout

    private lateinit var yLayout: TextInputLayout
    // the dropdown for the category
    private lateinit var categoryInput: MaterialAutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout
    // if we are editing this holds the id, null means we are adding a new one
    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()

        // the id gets passed in when editing, -1 means there wasnt one so its a new spot
        editingId = intent.getLongExtra("id", -1L)

        // if we got a real id, load that spot into the form
        if (editingId != -1L) {
            loadExistingSpot(editingId!!)
        }

        // the title bar says Add or Edit depending on what we are doing
        supportActionBar?.title =
            if (editingId == -1L) getString(R.string.add_spot) else getString(R.string.edit_spot)
    }


    // the up arrow in the toolbar just closes this screen and goes back
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun createUserInterface() {

        // pull the padding from dimens
        val pad = resources.getDimensionPixelSize(R.dimen.screen_padding)

        // vertical root layout
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        // the toolbar for this screen
        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.add_spot)
            setBackgroundColor(context.getColor(R.color.spot_primary))
            setTitleTextColor(context.getColor(R.color.white))
        }

            root.addView(
                toolbar,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            setSupportActionBar(toolbar)
            // show the little back arrow in the toolbar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // make the empty inputs, they get filled in later if we are editing
            titleInput = TextInputEditText(this)
            descriptionInput = TextInputEditText(this)

            // x and y only allow numbers (with a decimal point for the co ordinates)
            xInput = TextInputEditText(this).apply {
                inputType =
                    InputType.TYPE_CLASS_NUMBER or
                            InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            yInput = TextInputEditText(this).apply {
                inputType =
                    InputType.TYPE_CLASS_NUMBER or
                            InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        // wrap each input in a TextInputLayout so we get the floating hint
        titleLayout = TextInputLayout(this).apply {
            hint = getString(R.string.title_hint)
            addView(titleInput)
        }

        descriptionLayout = TextInputLayout(this).apply {
            hint = getString(R.string.description_hint)
            addView(descriptionInput)
        }

        xLayout = TextInputLayout(this).apply {
            hint = getString(R.string.x_hint)
            addView(xInput)
        }

        yLayout = TextInputLayout(this).apply {
            hint = getString(R.string.y_hint)
            addView(yInput)
        }

        // grab the category options straight out of arrays.xml
        val categories = resources.getStringArray(R.array.categories)

        // the dropdown itself, an AutoCompleteTextView shows the list when you tap it
        categoryInput = MaterialAutoCompleteTextView(this).apply {
            // TYPE_NULL stops the keyboard popping up, we only want the dropdown here
            inputType = InputType.TYPE_NULL
            setAdapter(ArrayAdapter(this@AddEditActivity, android.R.layout.simple_list_item_1, categories))
        }

        // wrap the dropdown too so it gets a floating hint + the little arrow icon
        categoryLayout = TextInputLayout(this).apply {
            hint = getString(R.string.category_hint)
            endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            addView(categoryInput)
        }

            // save button, runs saveSpot() when tapped
            val saveButton = MaterialButton(this).apply {
                text = getString(R.string.save)
                setOnClickListener {
                    saveSpot()
                }
            }

            // cancel just closes the screen without saving
            val cancelButton = MaterialButton(this).apply {
                text = getString(R.string.cancel)
                setOnClickListener {
                    finish()
                }
            }

            // add everything to the root in order
            root.addView(titleLayout)
            root.addView(descriptionLayout)
            root.addView(xLayout)
            root.addView(yLayout)
            root.addView(categoryLayout)
            root.addView(saveButton)
            root.addView(cancelButton)

            setContentView(root)
        }

    // fills the form with an existing spot when we open it to edit
    private fun loadExistingSpot(id: Long) {

        // ask the store for the spot, it might come back null
        val spot = AppData.studySpots.findOne(id)

        // if it doesnt exist just tell them and close the screen
        if (spot == null) {
            Toast.makeText(
                this,
                getString(R.string.spot_not_found),
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        // put the saved values back into the inputs
        titleInput.setText(spot.title)
        descriptionInput.setText(spot.desc)
        xInput.setText(spot.x.toString())
        yInput.setText(spot.y.toString())
        // false so it doesnt try to filter the dropdown while we set the text
        categoryInput.setText(spot.category, false)
    }

    // checks the form then saves it
    private fun saveSpot() {

        // read the values out, trim() gets rid of spaces at the ends
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        // title is required, show the error and stop if its empty
        if (title.isEmpty()) {
            titleLayout.error = getString(R.string.err_title_required)
            return
        }

        // x has to be a number, toDoubleOrNull gives null if you typed something silly
        val x = xInput.text.toString().toDoubleOrNull()

        if (x == null) {
            xLayout.error = getString(R.string.err_valid_number)
            return
        }

        // same check for y
        val y = yInput.text.toString().toDoubleOrNull()

        if (y == null) {
            yLayout.error = getString(R.string.err_valid_number)
            return
        }

        // the category is optional so no validation needed
        val category = categoryInput.text.toString()

        // no id means its a brand new spot
        if (editingId == null || editingId == -1L) {

            val spot = StudySpot(
                title = title,
                desc = description,
                x = x,
                y = y,
                category = category
            )

            // create it in the store (this also gives it an id)
            AppData.studySpots.create(spot)

            Toast.makeText(
                this,
                getString(R.string.spot_created),
                Toast.LENGTH_SHORT
            ).show()

        } else {
            // otherwise we are editing so we keep the same id

            val spot = StudySpot(
                id = editingId!!,
                title = title,
                desc = description,
                x = x,
                y = y,
                category = category
            )

            AppData.studySpots.update(spot)

            Toast.makeText(
                this,
                getString(R.string.spot_updated),
                Toast.LENGTH_SHORT
            ).show()
        }

        // close the screen, onResume of main will redraw the list
        finish()
    }
}
