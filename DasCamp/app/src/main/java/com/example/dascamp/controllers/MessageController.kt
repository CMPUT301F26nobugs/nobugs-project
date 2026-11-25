package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.OwnerMessage
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import java.util.UUID

/**
 * Messages between an entrant and the owner of a campsite, tied to one reservation.
 *
 * An entrant can write only about a reservation they hold. The campsite owner
 * can reply on that same thread. US 01.14.01
 */
class MessageController(private val store: CampStore) {

    /** Reservations the signed-in entrant can message about. */
    fun entrantThreads(): List<Reservation> {
        val user = store.currentUser ?: return emptyList()
        return store.reservations.filter { reservation ->
            reservation.entrantId == user.id && reservation.status != ReservationStatus.CANCELLED
        }
    }

    /**
     * Sends [body] on the thread for [reservationId].
     *
     * @return null on success, or a message explaining why it was rejected
     */
    fun send(reservationId: String, body: String): String? {
        val user = store.currentUser ?: return "Sign up first"
        val text = body.trim()
        if (text.isEmpty()) return "Write a message"

        val reservation = store.reservations.find { it.id == reservationId }
            ?: return "Choose a reservation first"
        if (reservation.status == ReservationStatus.CANCELLED) {
            return "This reservation was cancelled"
        }

        val campsite = store.campsites.find { it.id == reservation.campsiteId }
        val isEntrant = reservation.entrantId == user.id
        val isOwner = campsite?.ownerId == user.id
        if (!isEntrant && !isOwner) {
            return "You can message the owner only for a reservation you have"
        }

        val receiverId = if (isEntrant) campsite?.ownerId.orEmpty() else reservation.entrantId
        store.addMessage(
            OwnerMessage(
                id = UUID.randomUUID().toString(),
                reservationId = reservation.id,
                entrantName = reservation.entrantName,
                body = text,
                sentAt = System.currentTimeMillis().toString(),
                senderId = user.id,
                receiverId = receiverId
            )
        )
        return null
    }
}
