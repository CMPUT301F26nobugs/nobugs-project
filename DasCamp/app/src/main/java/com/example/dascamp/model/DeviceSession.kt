package com.example.dascamp.model

import android.content.Context

/**
 * The one profile this phone currently belongs to.
 *
 * A phone remembers a single person. When someone else claims it, the previous
 * person is no longer remembered. There is no username or password.
 * US 01.09.01
 */
object DeviceSession {
    private const val PREFS = "dascamp_session"
    private const val ACTIVE = "activeUserId"
    private const val CLAIMED = "claimedUserId"
    private const val SIGNED_OUT = "signedOut"
    private const val REMEMBER = "rememberDevice"

    /** Profile to open immediately, or null when the welcome screen should show. */
    fun activeUserId(context: Context, deviceId: String): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(SIGNED_OUT, false)) return null
        if (!prefs.getBoolean(REMEMBER, true)) return null
        return prefs.getString(CLAIMED, null)
            ?: prefs.getString(ACTIVE, null)
            ?: deviceId
    }

    /** The only profile offered on the welcome screen after someone has claimed the phone. */
    fun rememberedUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(CLAIMED, null) ?: prefs.getString(ACTIVE, null)
    }

    /**
     * Makes [userId] the only profile tied to this phone.
     * [remember] decides whether the next launch skips the welcome screen.
     */
    fun signIn(context: Context, userId: String, remember: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(SIGNED_OUT, false)
            .putBoolean(REMEMBER, remember)
            .putString(CLAIMED, userId)
            .putString(ACTIVE, userId)
            .apply()
    }

    /** Leaves the claimed profile in place, but requires the welcome screen next time. */
    fun signOut(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(SIGNED_OUT, true)
            .putBoolean(REMEMBER, false)
            .apply()
    }
}
