package com.example.dascamp.model

/**
 * A written review an entrant posts for a campsite they have reserved.
 *
 * [rating] is the 1-to-5 score that feeds the campsite average.
 * [ratingRemoved] is set by an admin when that score violates app policy;
 * the written [body] stays until the review itself is removed.
 *
 * Defaults keep a no-arg constructor so Firestore can call `toObject()`.
 *
 * Stories: US 01.13.02, US 03.09.01, US 03.10.01
 */
data class CampsiteReview(
    val id: String = "",
    val campsiteId: String = "",
    val entrantId: String = "",
    val entrantName: String = "",
    /** 1 to 5. Ignored once [ratingRemoved] is true. */
    val rating: Int = 0,
    val body: String = "",
    val createdAt: String = "",
    val ratingRemoved: Boolean = false
)
