package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.WaitlistEntrant

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
 *
 * Stories:
 * US 01.08.01
 */
class ProfileController(private val store: CampStore) {

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
}