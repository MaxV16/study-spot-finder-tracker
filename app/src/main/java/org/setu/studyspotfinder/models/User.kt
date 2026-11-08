package org.setu.studyspotfinder.models

// one account in the app. the password itself is never stored, only the hash and the salt used to make it.
data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val salt: String
)
