package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.CampsiteReview
import com.example.dascamp.model.ReservationStatus
import java.util.UUID
import kotlin.math.round

/**
 * Lets an entrant post one review for a campsite they have reserved,
 * and keeps that campsite's average rating in step with the reviews.
 *
 * Stories: US 01.13.02
 */
class ReviewController(private val store: CampStore) {

    /**
     * Posts a review, or replaces the signed-in entrant's earlier review
     * for the same campsite.
     *
     * @return null on success, or a message explaining why it was rejected
     */
    fun postReview(campsiteId: String, rating: Int, body: String): String? {
        val user = store.currentUser ?: return "Sign up first"
        if (rating !in 1..5) return "Choose a rating from 1 to 5"
        val text = body.trim()
        if (text.isEmpty()) return "Write a review"

        val hasReservation = store.reservations.any { reservation ->
            reservation.campsiteId == campsiteId &&
                reservation.entrantId == user.id &&
                reservation.status != ReservationStatus.CANCELLED
        }
        if (!hasReservation) {
            return "You can review a campsite only after you have a reservation there"
        }

        val existing = store.reviews.find { it.campsiteId == campsiteId && it.entrantId == user.id }
        val review = CampsiteReview(
            id = existing?.id ?: UUID.randomUUID().toString(),
            campsiteId = campsiteId,
            entrantId = user.id,
            entrantName = user.displayName().ifBlank { "Entrant" },
            rating = rating,
            body = text,
            createdAt = existing?.createdAt?.ifBlank { null } ?: System.currentTimeMillis().toString(),
            ratingRemoved = false
        )
        if (existing == null) store.addReview(review) else store.updateReview(review)
        refreshCampsiteRating(campsiteId)
        return null
    }

    /**
     * Rewrites the campsite's average rating from reviews whose scores
     * have not been removed by an admin.
     */
    fun refreshCampsiteRating(campsiteId: String) {
        val campsite = store.campsites.find { it.id == campsiteId } ?: return
        val counted = store.reviews.filter { review ->
            review.campsiteId == campsiteId && !review.ratingRemoved && review.rating in 1..5
        }
        val average = if (counted.isEmpty()) {
            0.0
        } else {
            round(counted.sumOf { it.rating } * 100.0 / counted.size) / 100.0
        }
        store.updateCampsite(campsite.copy(rating = average, ratingCount = counted.size))
    }
}
