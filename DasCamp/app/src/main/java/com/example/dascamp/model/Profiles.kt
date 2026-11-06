package com.example.dascamp.model

import com.google.firebase.firestore.Exclude

/**
 * The roles a user can hold. One user can have several at once.
 *
 * - [ENTRANT]: can enter lotteries for campsites.
 * - [OWNER]: can manage campsites and message entrants.
 * - [ADMIN]: has administrative privileges over the app.
 */
enum class Role { ENTRANT, OWNER, ADMIN }

/**
 * One person using the app, identified by their device id.
 *
 * Shared details live here once. A user's roles are stored as the boolean flags
 * [entrant], [owner], and [admin], and exposed as a set through [roles].
 *
 * All properties have defaults so the class has a no-arg constructor,
 * which is needed for deserialization (e.g. Firestore's `toObject()`).
 *
 * @property id unique identifier of the user (the device id)
 * @property firstName the user's first name
 * @property lastName the user's last name
 * @property email the user's email address
 * @property phoneNumber the user's phone number (empty string if not provided)
 * @property notificationsOn whether the user wants to receive notifications; defaults to true
 * @property entrant whether the user holds the [Role.ENTRANT] role
 * @property owner whether the user holds the [Role.OWNER] role
 * @property admin whether the user holds the [Role.ADMIN] role
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
    /**
     * The set of roles this user currently holds.
     *
     * Derived from [entrant], [owner], and [admin] each time it is read, and never
     * stored (`@get:Exclude` keeps it out of Firestore).
     */
    @get:Exclude
    val roles: Set<Role>
        get() = buildSet {
            if (entrant) add(Role.ENTRANT)
            if (owner) add(Role.OWNER)
            if (admin) add(Role.ADMIN)
        }

    /**
     * The user's full name for display, as "first last".
     *
     * Surrounding whitespace is trimmed, so if one name is blank, only the other is returned.
     */
    fun displayName(): String = "$firstName $lastName".trim()
}