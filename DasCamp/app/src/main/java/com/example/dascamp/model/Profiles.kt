package com.example.dascamp.model

import com.google.firebase.firestore.Exclude

/** The roles a user can hold. One user can have several at once */
enum class Role { ENTRANT, OWNER, ADMIN }

/**
 * One person using the app, identified by their device id
 * Shared details live here once. Roles come from [entrant], [owner], and [admin]
 */
data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val notificationsOn: Boolean = true,
    val entrant: Boolean = false,
    val owner: Boolean = false,
    val admin: Boolean = false
) {
    // Derived from entrant/owner/admin each time, never stored (@get:Exclude keeps it out of Firestore)
    @get:Exclude
    val roles: Set<Role>
        get() = buildSet {
            if (entrant) add(Role.ENTRANT)
            if (owner) add(Role.OWNER)
            if (admin) add(Role.ADMIN)
        }

    fun displayName(): String = "$firstName $lastName".trim()
}