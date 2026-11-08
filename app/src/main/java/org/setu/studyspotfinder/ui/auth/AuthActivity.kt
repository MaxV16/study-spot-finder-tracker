package org.setu.studyspotfinder.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.data.AuthManager
import org.setu.studyspotfinder.data.AuthResult
import org.setu.studyspotfinder.ui.MainActivity

// the login screen. same form does both, the button you tap decides if its a login or a signup.
// local accounts only, no internet needed so the demo cant break.
class AuthActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // already logged in? straight to the list, no point showing this again
        if (AuthManager.currentUser() != null) {
            goToMain()
            return
        }

        createUserInterface()
    }

    private fun createUserInterface() {
        val pad = resources.getDimensionPixelSize(R.dimen.screen_padding)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        val heading = TextView(this).apply {
            text = getString(R.string.auth_heading)
            textSize = 26f
        }

        emailInput = TextInputEditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val emailLayout = TextInputLayout(this).apply {
            hint = getString(R.string.email)
            addView(emailInput)
        }

        passwordInput = TextInputEditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val passwordLayout = TextInputLayout(this).apply {
            hint = getString(R.string.password)
            // little eye icon to show/hide what you typed
            endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            addView(passwordInput)
        }

        // errors show up here instead of a toast so the form stays put
        status = TextView(this).apply {
            setTextColor(getColor(R.color.spot_secondary))
            setPadding(0, pad / 2, 0, pad / 2)
        }

        val login = MaterialButton(this).apply {
            text = getString(R.string.login)
            setOnClickListener { submit(register = false) }
        }

        val register = MaterialButton(this).apply {
            text = getString(R.string.create_account)
            setOnClickListener { submit(register = true) }
        }

        root.addView(heading)
        root.addView(emailLayout)
        root.addView(passwordLayout)
        root.addView(status)
        root.addView(login)
        root.addView(register)

        setContentView(root)
    }

    private fun submit(register: Boolean) {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        when (val result = if (register) {
            AuthManager.signUp(email, password)
        } else {
            AuthManager.signIn(email, password)
        }) {
            is AuthResult.Success -> goToMain()
            is AuthResult.Error -> status.text = result.message
        }
    }

    private fun goToMain() {
        // clear the task so back doesnt drop you on the login screen again
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
