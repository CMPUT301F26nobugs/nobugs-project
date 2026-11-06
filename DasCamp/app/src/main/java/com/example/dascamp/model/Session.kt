package com.example.dascamp.model

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Who is using the app and which role they are acting as right now
 * The uid is the device id, so no username or password is needed
 */
class Session(private val repo: DasCampRepository, context: Context) {
    val uid: String = Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ANDROID_ID
    ) ?: ""

    var activeRole by mutableStateOf<Role?>(null)
        private set

    val roles: Set<Role> get() = repo.rolesOf(uid)

    fun chooseRole() {
        if (activeRole !in roles) activeRole = roles.firstOrNull()
    }

    fun switchRole(role: Role) {
        if (role in roles) activeRole = role
    }
}