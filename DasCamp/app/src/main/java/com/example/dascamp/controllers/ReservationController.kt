package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.Notification
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.WaitlistStatus
import java.util.UUID

/**
 * RESERVATION CONTROLLER
 *
 * Responsibilities:
 * - Reserve a campsite after winning a lottery, and send the confirmation
 * - Decline a won lottery spot
 * - List the signed-in entrant's active and past reservations
 * - Cancel a reservation
 *
 * Collaborators:
 * - DasCampstore (reservations, lotteries, notifications, current user)
 *
 * Stories: US 01.04.03, 01.05.01, 01.06.01, 01.06.02, 01.07.01
 */
class ReservationController(private val store: CampStore) {

    private fun today(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return formatter.format(java.util.Date())
    }
    private val dateFormat = Regex("\\d{4}-\\d{2}-\\d{2}")

    private fun mine() = store.reservations.filter { it.entrantId == store.uid }

    // Active: not cancelled and not finished yet
    fun active(): List<Reservation> =
        mine().filter { it.status == ReservationStatus.ACTIVE && it.endDate >= today() }

    // Past: finished stays (an ACTIVE one whose end date has passed counts too)
    fun past(): List<Reservation> =
        mine().filter {
            it.status == ReservationStatus.PAST ||
                    (it.status == ReservationStatus.ACTIVE && it.endDate < today())
        }

    // The lottery spot this entrant has won for a campsite, if any
    fun wonLottery(campsiteId: String): Lottery? =
        store.lotteries.firstOrNull { lottery ->
            campsiteId in lottery.campsiteIds &&
                    lottery.entrants.any {
                        it.entrantId == store.uid && it.status == WaitlistStatus.SELECTED
                    }
        }

    /** Returns an error message, or null when the reservation was made */
    fun reserve(lottery: Lottery, campsite: Campsite, start: String, end: String): String? {
        val user = store.currentUser ?: return "Sign up first"
        val entry = lottery.entrants.find { it.entrantId == user.id }
        if (entry == null || entry.status != WaitlistStatus.SELECTED) {
            return "You were not selected for this lottery"
        }
        if (today() < lottery.bookingStart || today() > lottery.bookingEnd) {
            return "Booking is not open right now"
        }
        if (!dateFormat.matches(start) || !dateFormat.matches(end)) {
            return "Use the format 2027-05-01 for both dates"
        }
        if (end < start) return "The end date has to be on or after the start date"
        if (campsite.availability.none { it.start <= start && end <= it.end }) {
            return "Those dates are not available at this campsite"
        }
        val clash = store.reservations.any {
            it.campsiteId == campsite.id && it.status == ReservationStatus.ACTIVE &&
                    it.startDate <= end && start <= it.endDate
        }
        if (clash) return "Those dates are already booked"

        store.addReservation(
            Reservation(
                id = UUID.randomUUID().toString(),
                campsiteId = campsite.id,
                campsiteName = campsite.name,
                entrantId = user.id,
                entrantName = user.displayName(),
                startDate = start,
                endDate = end,
                status = ReservationStatus.ACTIVE,
                lotteryId = lottery.id
            )
        )
        setEntryStatus(lottery, WaitlistStatus.BOOKED)
        notify(lottery.id, "Reservation confirmed: ${campsite.name}, $start to $end")
        return null
    }

    fun decline(lottery: Lottery) {
        setEntryStatus(lottery, WaitlistStatus.DECLINED)
    }

    fun cancel(reservation: Reservation) {
        if (reservation.entrantId != store.uid) return
        store.updateReservation(reservation.copy(status = ReservationStatus.CANCELLED))
        notify(reservation.lotteryId, "Reservation cancelled: ${reservation.campsiteName}, " +
                "${reservation.startDate} to ${reservation.endDate}")
    }

    private fun setEntryStatus(lottery: Lottery, status: WaitlistStatus) {
        store.updateLottery(
            lottery.copy(entrants = lottery.entrants.map {
                if (it.entrantId == store.uid) it.copy(status = status) else it
            })
        )
    }

    private fun notify(lotteryId: String?, message: String) {
        val user = store.currentUser ?: return
        store.addNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                lotteryId = lotteryId,
                recipientName = user.displayName(),
                recipientId = user.id,
                message = message,
                sentAt = System.currentTimeMillis().toString()
            )
        )
    }
    /**
     * Active reservations for campsites owned by the signed-in owner.
     *
     * @return reservations that are ACTIVE and have not ended yet
     */
    fun ownerActive(): List<Reservation> {
        val mySites = store.campsites.filter { it.ownerId == store.uid }.map { it.id }.toSet()
        return store.reservations.filter {
            it.campsiteId in mySites &&
                    it.status == ReservationStatus.ACTIVE &&
                    it.endDate >= today()
        }
    }

    /**
     * Past reservations for campsites owned by the signed-in owner.
     *
     * @return finished stays, including ACTIVE ones whose end date has passed
     */
    fun ownerPast(): List<Reservation> {
        val mySites = store.campsites.filter { it.ownerId == store.uid }.map { it.id }.toSet()
        return store.reservations.filter {
            it.campsiteId in mySites && (
                    it.status == ReservationStatus.PAST ||
                            (it.status == ReservationStatus.ACTIVE && it.endDate < today())
                    )
        }
    }

    /**
     * Cancelled reservations for campsites owned by the signed-in owner.
     *
     * @return reservations with CANCELLED status
     */
    fun ownerCancelled(): List<Reservation> {
        val mySites = store.campsites.filter { it.ownerId == store.uid }.map { it.id }.toSet()
        return store.reservations.filter {
            it.campsiteId in mySites && it.status == ReservationStatus.CANCELLED
        }
    }

}
