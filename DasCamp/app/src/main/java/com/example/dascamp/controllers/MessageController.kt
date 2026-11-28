package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.OwnerMessage
import com.example.dascamp.model.Reservation
import java.util.UUID

/** One owner/entrant conversation, represented by its latest message. */
data class MessageConversationSummary(
    val ownerId: String,
    val entrantId: String,
    val otherName: String,
    val latestMessage: OwnerMessage
)

private data class MessagePairContext(
    val message: OwnerMessage,
    val ownerId: String,
    val entrantId: String
)

/**
 * Coordinates reservation- and lottery-linked messages. Messages from all
 * reservations and lotteries for one owner/entrant pair share one conversation.
 */
class MessageController(private val store: CampStore) {

    /** Existing conversations for the signed-in entrant, newest first. */
    fun entrantConversations(): List<MessageConversationSummary> =
        conversationsFor(isEntrant = true)

    /** Existing conversations for the signed-in owner, newest first. */
    fun ownerConversations(): List<MessageConversationSummary> =
        conversationsFor(isEntrant = false)

    private fun conversationsFor(isEntrant: Boolean): List<MessageConversationSummary> {
        val user = store.currentUser ?: return emptyList()
        return store.messages.mapNotNull { message ->
            val pair = participants(message) ?: return@mapNotNull null
            pair to message
        }.groupBy({ it.first }, { it.second })
            .mapNotNull { (pair, messages) ->
                val (ownerId, entrantId) = pair
                val belongsToUser = if (isEntrant) entrantId == user.id else ownerId == user.id
                if (!belongsToUser) return@mapNotNull null

                val latest = messages.maxByOrNull {
                    it.orderingTimeMillis()
                } ?: return@mapNotNull null
                val otherName = if (isEntrant) {
                    store.userById(ownerId)?.displayName()
                        ?.takeIf { it.isNotBlank() } ?: "Campsite owner"
                } else {
                    latest.entrantName.ifBlank {
                        store.userById(entrantId)?.displayName().orEmpty()
                    }.ifBlank { "Entrant" }
                }

                MessageConversationSummary(ownerId, entrantId, otherName, latest)
            }
            .sortedByDescending { it.latestMessage.orderingTimeMillis() }
    }

    /**
     * Sends [body] in the owner/entrant conversation. A first message must be
     * backed by a lottery participation or reservation; an existing conversation
     * remains replyable even if the participant later leaves or a reservation is
     * cancelled.
     *
     * @return null on success, or a message explaining why it was rejected
     */
    fun send(
        ownerId: String,
        entrantId: String,
        body: String,
        lotteryId: String? = null,
        reservationId: String? = null,
        fromOwner: Boolean = false
    ): String? {
        val user = store.currentUser ?: return "Sign up first"
        val text = body.trim()
        if (text.isEmpty()) return "Write a message"
        if (user.id != ownerId && user.id != entrantId) {
            return "You can only send messages as part of this conversation"
        }

        val lottery = lotteryId?.let { id -> store.lotteries.find { it.id == id } }
        val lotteryAllowsPair = lottery?.let { selectedLottery ->
            val ownerIncluded = selectedLottery.campsiteIds.any { campsiteId ->
                store.campsites.any { it.id == campsiteId && it.ownerId == ownerId }
            }
            val entrantIncluded = selectedLottery.entrants.any { it.entrantId == entrantId }
            ownerIncluded && entrantIncluded
        } == true

        val reservation = reservationId?.let { id ->
            store.reservations.find { it.id == id }
        }
        val reservationAllowsPair = reservation?.let { selectedReservation ->
            selectedReservation.entrantId == entrantId &&
                store.campsites.any {
                    it.id == selectedReservation.campsiteId && it.ownerId == ownerId
                }
        } == true

        val conversationAlreadyExists = store.messages.any { message ->
            participants(message) == (ownerId to entrantId)
        }
        if (!lotteryAllowsPair && !reservationAllowsPair && !conversationAlreadyExists) {
            return "You can message only someone connected to your lottery or reservation"
        }

        val entrantName = reservation?.entrantName
            ?: lottery?.entrants?.firstOrNull { it.entrantId == entrantId }?.name
            ?: store.userById(entrantId)?.displayName().orEmpty()
        val receiverId = if (user.id == entrantId) ownerId else entrantId
        store.addMessage(
            OwnerMessage(
                id = UUID.randomUUID().toString(),
                reservationId = reservation?.id.orEmpty(),
                ownerId = ownerId,
                entrantId = entrantId,
                entrantName = entrantName,
                body = text,
                sentAt = System.currentTimeMillis().toString(),
                senderId = user.id,
                receiverId = receiverId,
                fromOwner = fromOwner
            )
        )
        return null
    }

    /** Legacy reservation entry point, retained for existing callers. */
    fun send(reservationId: String, body: String): String? {
        val reservation: Reservation = store.reservations.find { it.id == reservationId }
            ?: return "Choose a reservation first"
        val campsite = store.campsites.find { it.id == reservation.campsiteId }
            ?: return "Campsite not found"
        return send(
            ownerId = campsite.ownerId,
            entrantId = reservation.entrantId,
            body = body,
            reservationId = reservation.id
        )
    }

    /** Resolves older reservation-linked messages that predate explicit pair IDs. */
    private fun participants(message: OwnerMessage): Pair<String, String>? {
        if (message.ownerId.isNotBlank() && message.entrantId.isNotBlank()) {
            return message.ownerId to message.entrantId
        }
        val reservation = store.reservations.find { it.id == message.reservationId }
            ?: return null
        val ownerId = store.campsites.find { it.id == reservation.campsiteId }?.ownerId
            ?.takeIf { it.isNotBlank() }
            ?: return null
        val entrantId = reservation.entrantId.takeIf { it.isNotBlank() } ?: return null
        return ownerId to entrantId
    }
}
