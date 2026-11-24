package com.example.dascamp.controllers

import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.OwnerApplicationStatus
import com.example.dascamp.model.User

/**
 * ADMIN CONTROLLER
 * TO BE IMPLEMENTED
 */
class AdminController(private val repository: DasCampRepository) {
    /** Approve a user's application to become an owner */
    fun approveOwner(applicant: User) {
        repository.updateUser(
            applicant.copy(
                owner = true,
                ownerApplication = OwnerApplicationStatus.APPROVED
            )
        )
    }

    /** Deny a user's application to become an owner */
    fun rejectOwner(applicant: User) {
        repository.updateUser(
            applicant.copy(
                ownerApplication = OwnerApplicationStatus.DENIED
            )
        )
    }

    /** Remove a campsite owner */
    fun revokeOwner(applicant: User) {
        repository.updateUser(
            applicant.copy(
                owner = false,
                ownerApplication = OwnerApplicationStatus.EMPTY
            )
        )
    }
}