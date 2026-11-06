package com.example.dascamp.model

/**
 * The lifecycle state of a [Reservation].
 *
 * - [ACTIVE]: the reservation is current or upcoming and has not been rejected.
 * - [PAST]: the reservation's dates have passed.
 * - [CANCELLED]: the reservation was cancelled, e.g. rejected by the campsite owner.
 */
enum class ReservationStatus { ACTIVE, PAST, CANCELLED }

/**
 * Returns the user-facing label for this status, e.g. `"Active"`.
 */
fun ReservationStatus.label(): String = when (this) {
    ReservationStatus.ACTIVE -> "Active"
    ReservationStatus.PAST -> "Past"
    ReservationStatus.CANCELLED -> "Cancelled"
}

/**
 * A reservation created after an entrant is confirmed a spot at a campsite.
 *
 * The owner reviews reservations and can reject an active one. [cancelReason] is
 * the explanation the owner sends along with a rejection.
 *
 * All properties have defaults so the class has a no-arg constructor,
 * which is needed for deserialization (e.g. Firestore's `toObject()`).
 *
 * @property id unique identifier of the reservation
 * @property campsiteId id of the reserved campsite
 * @property campsiteName display name of the campsite (a denormalized copy)
 * @property entrantId id of the user who holds the reservation
 * @property entrantName display name of that user (a denormalized copy)
 * @property startDate first day of the stay, in `yyyy-MM-dd` format
 * @property endDate last day of the stay, in `yyyy-MM-dd` format
 * @property status current lifecycle state; defaults to [ReservationStatus.ACTIVE]
 * @property lotteryId id of the lottery that produced this reservation, or null if it
 * didn't come from one
 * @property cancelReason the owner's reason for rejecting the reservation, or null if
 * it hasn't been cancelled
 * @property guestCount number of guests in the party; defaults to 1
 * @property rulesAccepted whether the entrant accepted the campsite rules
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