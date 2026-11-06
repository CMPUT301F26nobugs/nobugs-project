package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore

/**
 * PROFILE CONTROLLER
 *
 * Responsibilities:
 * - Handle profile actions from the screens (edit profile, toggle notifications)
 * - Pass the changes to the model
 * - Change what roles the signed-in user holds
 * - Update notification preference
 * - Delete the signed-in user's profile
 *
 * Collaborators:
 * - CampStore (current user)
 *
 * Used by screens: EntrantProfileScreen
 */
class ProfileController(private val store: CampStore) {

    fun becomeOwner() {
        val user = store.currentUser ?: return
        if (!user.owner) store.updateUser(user.copy(owner = true))
    }

    fun setNotifications(on: Boolean) {
        val user = store.currentUser ?: return
        store.updateUser(user.copy(notificationsOn = on))
    }

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