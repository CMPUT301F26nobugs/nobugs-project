package com.example.dascamp.controllers

import com.example.dascamp.model.Campsite
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.Notification
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.WaitlistStatus
import java.time.LocalDate
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
 * - DasCampRepository (reservations, lotteries, notifications, current user)
 *
 * Stories: US 01.04.03, 01.05.01, 01.06.01, 01.06.02, 01.07.01
 */
class ReservationController(private val repository: DasCampRepository) {

    private fun today() = LocalDate.now().toString()   // yyyy-MM-dd
    private val dateFormat = Regex("\\d{4}-\\d{2}-\\d{2}")

    private fun mine() = repository.reservations.filter { it.entrantId == repository.uid }

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
        repository.lotteries.firstOrNull { lottery ->
            campsiteId in lottery.campsiteIds &&
                    lottery.entrants.any {
                        it.entrantId == repository.uid && it.status == WaitlistStatus.SELECTED
                    }
        }

    /** Returns an error message, or null when the reservation was made */
    fun reserve(lottery: Lottery, campsite: Campsite, start: String, end: String): String? {
        val user = repository.currentUser ?: return "Sign up first"
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
        val clash = repository.reservations.any {
            it.campsiteId == campsite.id && it.status == ReservationStatus.ACTIVE &&
                    it.startDate <= end && start <= it.endDate
        }
        if (clash) return "Those dates are already booked"

        repository.addReservation(
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
        if (reservation.entrantId != repository.uid) return
        repository.updateReservation(reservation.copy(status = ReservationStatus.CANCELLED))
        notify(reservation.lotteryId, "Reservation cancelled: ${reservation.campsiteName}, " +
                "${reservation.startDate} to ${reservation.endDate}")
    }

    private fun setEntryStatus(lottery: Lottery, status: WaitlistStatus) {
        repository.updateLottery(
            lottery.copy(entrants = lottery.entrants.map {
                if (it.entrantId == repository.uid) it.copy(status = status) else it
            })
        )
    }

    private fun notify(lotteryId: String?, message: String) {
        val user = repository.currentUser ?: return
        repository.addNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                lotteryId = lotteryId,
                recipientName = user.displayName(),
                message = message,
                sentAt = System.currentTimeMillis().toString()
            )
        )
    }
}