package org.setu.studyspotfinder


import android.widget.ArrayAdapter
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class AddEditActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText

    private lateinit var titleLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout

    private lateinit var xLayout: TextInputLayout

    private lateinit var yLayout: TextInputLayout
    private lateinit var categoryInput: MaterialAutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout
    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()

        editingId = intent.getLongExtra("id", -1L)

        if (editingId != -1L) {
            loadExistingSpot(editingId!!)
        }

        supportActionBar?.title =
            if (editingId == -1L) getString(R.string.add_spot) else getString(R.string.edit_spot)
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

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
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            titleInput = TextInputEditText(this)
            descriptionInput = TextInputEditText(this)

            xInput = TextInputEditText(this).apply {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            yInput = TextInputEditText(this).apply {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
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

        val categories = resources.getStringArray(R.array.categories)

        categoryInput = MaterialAutoCompleteTextView(this).apply {
            inputType = android.text.InputType.TYPE_NULL
            setAdapter(ArrayAdapter(this@AddEditActivity, android.R.layout.simple_list_item_1, categories))
        }

        categoryLayout = TextInputLayout(this).apply {
            hint = getString(R.string.category_hint)
            endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            addView(categoryInput)
        }

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

    private fun loadExistingSpot(id: Long) {

        val spot = AppData.studySpots.findOne(id)

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
        xInput.setText(spot.x.toString())
        yInput.setText(spot.y.toString())
        categoryInput.setText(spot.category, false)
    }

    private fun saveSpot() {

        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        if (title.isEmpty()) {
            titleLayout.error = getString(R.string.err_title_required)
            return
        }

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

        finish()
    }
}
