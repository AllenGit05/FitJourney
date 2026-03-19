package com.example.fitjourney.data.manager

/**
 * Singleton object to track if the current app session is unlocked via biometrics.
 */
object AuthSession {
    var isUnlocked: Boolean = false
}
