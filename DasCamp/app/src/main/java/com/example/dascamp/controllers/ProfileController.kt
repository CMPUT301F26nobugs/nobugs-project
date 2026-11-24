package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.OwnerApplicationStatus
import com.example.dascamp.model.User

/**
 * PROFILE CONTROLLER
 *
 * Responsibilities:
 * - Change what roles the signed-in user holds
 * - Update notification preference
 * - Delete the signed-in user's profile
 *
 * Collaborators:
 * - DasCampstore (current user)
 */
class ProfileController(private val store: CampStore) {

    /** Entrant side: can they apply to be an owner */
    fun canApply(user: User? = store.currentUser): Boolean =
        user != null && !user.owner &&
                (user.ownerApplication == OwnerApplicationStatus.EMPTY ||
                 user.ownerApplication == OwnerApplicationStatus.DENIED)

    /** Entrant side: submit an application to become an owner */
    fun applyToBeOwner() {
        val user = store.currentUser ?: return
        if (!canApply(user)) return
        store.updateUser(
            user.copy(ownerApplication = OwnerApplicationStatus.PENDING)
        )
    }

    // Lets the signed-in user act as an owner (admins included). Entrant stays on
    fun becomeOwner() {
        val user = store.currentUser ?: return
        if (!user.owner) store.updateUser(user.copy(owner = true))
    }

    // Opt in or out of notifications
    fun setNotifications(on: Boolean) {
        val user = store.currentUser ?: return
        store.updateUser(user.copy(notificationsOn = on))
    }

    // Removes the profile. DasCampApp sends the user to sign-up once it is gone
    fun deleteProfile() {
        val user = store.currentUser ?: return
        store.deleteUser(user)
    }

    fun saveProfile(
        first: String,
        last: String,
        email: String,
        phone: String
    ) {
        val user = store.currentUser ?: return
        store.updateUser(
            user.copy(
                firstName = first.trim(),
                lastName = last.trim(),
                email = email.trim(),
                phoneNumber = phone.trim()
            )
        )
    }
}