package com.example.dascamp.controllers

import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.EntrantProfile

/**
 * PROFILE CONTROLLER
 *
 * Responsibilities:
 * - Handle profile actions from the screens (edit profile, toggle notifications)
 * - Pass the changes to the model
 *
 * Collaborators: DasCampRepository, EntrantProfile
 *
 * Used by screens: EntrantProfileScreen
 */
class ProfileController(private val repository: DasCampRepository) {

    fun saveEntrantProfile(
        current: EntrantProfile,
        first: String,
        last: String,
        email: String,
        phone: String
    ) {
        repository.updateEntrant(
            current.copy(
                firstName = first.trim(),
                lastName = last.trim(),
                email = email.trim(),
                phoneNumber = phone.trim()
            )
        )
    }
}