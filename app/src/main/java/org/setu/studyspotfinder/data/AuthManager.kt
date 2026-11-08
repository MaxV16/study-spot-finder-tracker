package org.setu.studyspotfinder.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import org.setu.studyspotfinder.models.User

// tells the screen if it worked or why it didnt
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

// one place for signup, login and the session. the session is just the user id in shared prefs,
// so closing the app and opening it again keeps you logged in.
object AuthManager {

    private lateinit var users: UserStore
    private lateinit var prefs: SharedPreferences

    private const val PREFS = "study_spot_session"
    private const val KEY_USER_ID = "logged_in_user_id"

    fun init(context: Context) {
        users = UserStore(context.applicationContext)
        prefs = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun signUp(email: String, password: String): AuthResult {
        validate(email, password)?.let { return AuthResult.Error(it) }

        return try {
            val user = users.create(email, password)
            saveSession(user)
            AuthResult.Success(user)
        } catch (e: IllegalArgumentException) {
            AuthResult.Error(e.message ?: "Could not create the account.")
        }
    }

    fun signIn(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Enter your email and password.")
        }

        val user = users.verify(email, password)
            ?: return AuthResult.Error("Email or password is incorrect.")

        saveSession(user)
        return AuthResult.Success(user)
    }

    fun currentUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        return users.findById(id)
    }

    fun signOut() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    private fun saveSession(user: User) {
        prefs.edit().putString(KEY_USER_ID, user.id).apply()
    }

    // checks before we even touch the store
    private fun validate(email: String, password: String): String? {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "Enter a valid email address."
        }
        if (password.length < 6) {
            return "Password must be at least 6 characters."
        }
        return null
    }
}
