package com.example.dascamp.model

enum class ReservationStatus { ACTIVE, PAST, CANCELLED }

fun ReservationStatus.label(): String = when (this) {
    ReservationStatus.ACTIVE -> "Active"
    ReservationStatus.PAST -> "Past"
    ReservationStatus.CANCELLED -> "Cancelled"
}

/**
 * Reservation after the entrant is confirmed a spot, The owner reviews them and can reject an active one
 * [cancelReason] is what the owner sends with a rejection.
 */
data class Reservation(
    val id: String = "",
    val campsiteId: String = "",
    val campsiteName: String = "",
    val entrantId: String = "",
    val entrantName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: ReservationStatus = ReservationStatus.ACTIVE,
    val lotteryId: String? = null,
    val cancelReason: String? = null,
    val guestCount: Int = 1,
    val rulesAccepted: Boolean = false
)

