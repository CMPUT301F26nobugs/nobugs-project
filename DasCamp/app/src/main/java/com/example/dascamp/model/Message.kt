package com.example.dascamp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * A message exchanged between a campsite owner and an entrant.
 * [ownerId] and [entrantId] identify the shared conversation. [reservationId]
 * remains populated for messages sent from an existing reservation.
 *
 * Fields have defaults so Firestore can deserialize documents created before
 * the server timestamp field was added.
 */
data class OwnerMessage(
    val id: String = "",
    val reservationId: String = "",
    val ownerId: String = "",
    val entrantId: String = "",
    val entrantName: String = "",
    val body: String = "",
    val sentAt: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val fromOwner: Boolean = false, //if owner and entrant have the same id/are the same person -> same account
    @get:ServerTimestamp val serverSentAt: Timestamp? = null
) {
    /** Server time for new messages, with the legacy client timestamp as fallback. */
    fun orderingTimeMillis(): Long =
        serverSentAt?.toDate()?.time ?: sentAt.toLongOrNull() ?: 0L
}
