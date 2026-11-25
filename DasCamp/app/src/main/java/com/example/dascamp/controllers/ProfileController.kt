package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.OwnerApplicationStatus
import com.example.dascamp.model.User
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.WaitlistEntrant

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
 * - DasCampstore (current user)
 *
 * Stories:
 * US 01.08.01
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

    fun setNotifications(on: Boolean) {
        val user = store.currentUser ?: return
        store.updateUser(user.copy(notificationsOn = on))
    }

    fun deleteProfile() {
        val user = store.currentUser ?: return
        for (reservation in store.reservations.toList()){
            if(reservation.entrantId == user.id && reservation.status == ReservationStatus.ACTIVE){
                store.updateReservation(reservation.copy(status = ReservationStatus.CANCELLED))
            }
        }

        for (lottery in store.lotteries.toList()){
            val keep = mutableListOf<WaitlistEntrant>()
            for (entrant in lottery.entrants){
                if (entrant.entrantId != user.id){
                    keep.add(entrant)
                }
            }
            if (keep.size < lottery.entrants.size){
                store.updateLottery(lottery.copy(entrants = keep))
            }
        }

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

    /**
     * Saves or replaces the signed-in user's profile picture.
     *
     * [path] is a file this app already copied into its private storage.
     * Pass null to clear the picture. US 01.08.02
     */
    fun setProfileImage(path: String?) {
        val user = store.currentUser ?: return
        val stored = path?.trim()?.ifBlank { null }
        store.updateUser(user.copy(profileImageUri = stored))
    }
}