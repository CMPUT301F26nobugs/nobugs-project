package com.example.dascamp.model

/**
 * A message exchanged between a campsite owner and an entrant about a reservation.
 *
 * All properties have empty-string defaults so the class has a no-arg constructor,
 * which is needed for deserialization (e.g. Firestore's `toObject()`).
 */
data class OwnerMessage(
    val id: String = "",
    val reservationId: String = "",
    val entrantName: String = "",
    val body: String = "",
    val sentAt: String = "",
    val senderId: String = "",
    val receiverId: String = ""
)
