package com.example.dascamp.controllers

import com.example.dascamp.model.DasCampRepository

/**
 * PROFILE CONTROLLER
 *
 * Responsibilities:
 * - Change what roles the signed-in user holds
 * - Update notification preference
 * - Delete the signed-in user's profile
 *
 * Collaborators:
 * - DasCampRepository (current user)
 */
class ProfileController(private val repository: DasCampRepository) {

    // Lets the signed-in user act as an owner (admins included). Entrant stays on
    fun becomeOwner() {
        val user = repository.currentUser ?: return
        if (!user.owner) repository.updateUser(user.copy(owner = true))
    }

    // Opt in or out of notifications
    fun setNotifications(on: Boolean) {
        val user = repository.currentUser ?: return
        repository.updateUser(user.copy(notificationsOn = on))
    }

    // Removes the profile. DasCampApp sends the user to sign-up once it is gone
    fun deleteProfile() {
        val user = repository.currentUser ?: return
        repository.deleteUser(user)
    }
}