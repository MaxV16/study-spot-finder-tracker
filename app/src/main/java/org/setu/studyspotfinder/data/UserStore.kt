package org.setu.studyspotfinder.data

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import org.setu.studyspotfinder.models.User
import java.io.File
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// keeps every account in users.json, same idea as StudySpotStore but for users.
// passwords never go in the file, only a pbkdf2 hash and a random salt.
class UserStore(context: Context) {

    private val file = File(context.filesDir, "users.json")

    fun findAll(): List<User> {
        if (!file.exists()) return emptyList()

        val array = JSONArray(file.readText())
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            User(
                id = obj.getString("id"),
                email = obj.getString("email"),
                passwordHash = obj.getString("passwordHash"),
                salt = obj.getString("salt")
            )
        }
    }

    fun findById(id: String): User? = findAll().find { it.id == id }

    fun findByEmail(email: String): User? =
        findAll().find { it.email.equals(email.trim(), ignoreCase = true) }

    fun create(email: String, password: String): User {
        val cleanEmail = email.trim().lowercase()
        require(findByEmail(cleanEmail) == null) { "An account with that email already exists." }

        // a new random salt every time so two people with the same password dont get the same hash
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)

        val user = User(
            id = UUID.randomUUID().toString(),
            email = cleanEmail,
            passwordHash = hash(password, saltBytes),
            salt = salt
        )

        val list = findAll().toMutableList()
        list.add(user)
        save(list)
        return user
    }

    // hashes what was typed with the stored salt and compares, thats how we check a login
    fun verify(email: String, password: String): User? {
        val user = findByEmail(email) ?: return null
        val saltBytes = Base64.decode(user.salt, Base64.NO_WRAP)
        val candidate = hash(password, saltBytes)

        return if (candidate == user.passwordHash) user else null
    }

    private fun hash(password: String, salt: ByteArray): String {
        // sha1 version because minSdk 24 does not always have pbkdf2withhmacsha256
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val bytes = factory.generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun save(list: List<User>) {
        val array = JSONArray()
        list.forEach { user ->
            array.put(
                JSONObject().apply {
                    put("id", user.id)
                    put("email", user.email)
                    put("passwordHash", user.passwordHash)
                    put("salt", user.salt)
                }
            )
        }
        file.writeText(array.toString())
    }
}
