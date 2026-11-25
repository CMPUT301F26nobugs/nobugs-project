package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.OwnerApplicationStatus
import com.example.dascamp.model.User

/**
 * Admin actions: moderate ratings and reviews, and decide owner applications.
 *
 * Stories: US 03.07.01, US 03.09.01, US 03.10.01
 */
class AdminController(private val store: CampStore) {

    /**
     * Drops a review's star rating so it no longer affects the campsite average.
     * The written review stays visible.
     *
     * @return null on success, or a message if there is nothing to remove
     */
    fun removeRating(reviewId: String): String? {
        val review = store.reviews.find { it.id == reviewId } ?: return "Review not found"
        if (review.ratingRemoved) return "That rating is already removed"
        store.updateReview(review.copy(ratingRemoved = true))
        ReviewController(store).refreshCampsiteRating(review.campsiteId)
        return null
    }

    /**
     * Deletes a review that violates app policy, then recalculates the average.
     *
     * @return null on success, or a message if the review is already gone
     */
    fun removeReview(reviewId: String): String? {
        val review = store.reviews.find { it.id == reviewId } ?: return "Review not found"
        val campsiteId = review.campsiteId
        store.deleteReview(review)
        ReviewController(store).refreshCampsiteRating(campsiteId)
        return null
    }

    /** Approve a user's application to become an owner. */
    fun approveOwner(applicant: User) {
        store.updateUser(
            applicant.copy(
                owner = true,
                ownerApplication = OwnerApplicationStatus.APPROVED
            )
        )
    }

    /** Deny a user's application to become an owner. */
    fun rejectOwner(applicant: User) {
        store.updateUser(
            applicant.copy(ownerApplication = OwnerApplicationStatus.DENIED)
        )
    }

    /** Remove a campsite owner. */
    fun revokeOwner(applicant: User) {
        store.updateUser(
            applicant.copy(
                owner = false,
                ownerApplication = OwnerApplicationStatus.EMPTY
            )
        )
    }
}
