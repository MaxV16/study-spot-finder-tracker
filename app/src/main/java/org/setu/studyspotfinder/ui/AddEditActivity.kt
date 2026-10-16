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

// this one screen does double duty, it adds a spot and edits one.
// how does it know wich? the id extra in the intent, no id = add, an id = edit that spot.
class AddEditActivity : AppCompatActivity() {

    // the actual input boxes
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText

    // the material wrappers around em, they give the floating label and the red error text
    private lateinit var titleLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout

    private lateinit var xLayout: TextInputLayout

    private lateinit var yLayout: TextInputLayout
    private lateinit var categoryInput: MaterialAutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout
    // null means we are adding, if its set we are editing that spot
    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()

        // read the id main passed in, nobody passed one so we get -1L back.
        // -1L means no id, so we know we are adding a brand new spot.
        editingId = intent.getLongExtra("id", -1L)

        // if we do have a real id, fill the form with that spots current values
        if (editingId != -1L) {
            loadExistingSpot(editingId!!)
        }

        supportActionBar?.title =
            if (editingId == -1L) getString(R.string.add_spot) else getString(R.string.edit_spot)
    }


    // the up arrow in the toolbar, finish() closes this screen and goes back to MainActivity
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // build the form
    private fun createUserInterface() {

        val pad = resources.getDimensionPixelSize(R.dimen.screen_padding)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

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
        // this shows the back/up arrow in the bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        titleInput = TextInputEditText(this)
        descriptionInput = TextInputEditText(this)

        // x and y only take numbers with decimals so the keyboard and validation match up
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

        // each input gets wrapped in a TextInputLayout, thats what gives it the floating hint
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

        // the category dropdown, we pull the options out of res/values/arrays.xml
        val categories = resources.getStringArray(R.array.categories)

        categoryInput = MaterialAutoCompleteTextView(this).apply {
            // TYPE_NULL stops the normal keyboard popping up, we want the dropdown instead
            inputType = InputType.TYPE_NULL
            // the adapter feeds the options to the dropdown
            setAdapter(ArrayAdapter(this@AddEditActivity, android.R.layout.simple_list_item_1, categories))
        }

        categoryLayout = TextInputLayout(this).apply {
            hint = getString(R.string.category_hint)
            // this draws the little dropdown arrow on the end of the field
            endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            addView(categoryInput)
        }

        // Save runs saveSpot(), Cancel just closes the screen
        val saveButton = MaterialButton(this).apply {
            text = getString(R.string.save)
            setOnClickListener {
                saveSpot()
            }
        }

        val cancelButton = MaterialButton(this).apply {
            text = getString(R.string.cancel)
            setOnClickListener {
                finish()
            }
        }

        root.addView(titleLayout)
        root.addView(descriptionLayout)
        root.addView(xLayout)
        root.addView(yLayout)
        root.addView(categoryLayout)
        root.addView(saveButton)
        root.addView(cancelButton)

        setContentView(root)
    }

    // grab the spot from the store and push its values into the fields
    private fun loadExistingSpot(id: Long) {

        val spot = AppData.studySpots.findOne(id)

        // unlikely but if the spot is gone, eg deleted somewhere else, tell the user and bail out
        if (spot == null) {
            Toast.makeText(
                this,
                getString(R.string.spot_not_found),
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        titleInput.setText(spot.title)
        descriptionInput.setText(spot.desc)
        // numbers get turned into text to go in the box
        xInput.setText(spot.x.toString())
        yInput.setText(spot.y.toString())
        // the false here means dont run the dropdown filter, we just wanna set the value
        categoryInput.setText(spot.category, false)
    }

    // runs when Save is tapped, validate eveything then either create or update
    private fun saveSpot() {

        // trim removes accidental spaces at the start/end
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        // no title means show the error under the box and stop
        if (title.isEmpty()) {
            titleLayout.error = getString(R.string.err_title_required)
            return
        }

        // toDoubleOrNull gives us the number, or null if what was typed isnt a number
        val x = xInput.text.toString().toDoubleOrNull()

        if (x == null) {
            xLayout.error = getString(R.string.err_valid_number)
            return
        }

        val y = yInput.text.toString().toDoubleOrNull()

        if (y == null) {
            yLayout.error = getString(R.string.err_valid_number)
            return
        }

        val category = categoryInput.text.toString()

        // no editingId means its a new spot so create it, the store gives it an id
        if (editingId == null || editingId == -1L) {

            val spot = StudySpot(
                title = title,
                desc = description,
                x = x,
                y = y,
                category = category
            )

            AppData.studySpots.create(spot)

            Toast.makeText(
                this,
                getString(R.string.spot_created),
                Toast.LENGTH_SHORT
            ).show()

        } else {

            // we have an id so its an edit, keep the same id and update the stored spot
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

        // close the form, we go back to MainActivity wich refreshes the list in onResume
        finish()
    }
}
