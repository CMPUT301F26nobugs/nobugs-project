package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.Notification
import com.example.dascamp.model.NotificationType
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
        if (lottery.status != LotteryStatus.DRAWN) {
            return "This lottery is not accepting bookings"
        }
        if (today() > lottery.bookingEnd) return "The booking period has ended"
        if (entry.responseDeadlineAt?.let { System.currentTimeMillis() >= it } == true) {
            return "Your response period has ended"
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
        val entry = lottery.entrants.firstOrNull { it.entrantId == store.uid } ?: return
        if (entry.status != WaitlistStatus.SELECTED) return
        if (entry.responseDeadlineAt?.let { System.currentTimeMillis() >= it } == true) return
        if (today() > lottery.bookingEnd) return
        setEntryStatus(lottery, WaitlistStatus.DECLINED)
    }

    fun cancel(reservation: Reservation) {
        if (reservation.entrantId != store.uid) return
        store.updateReservation(reservation.copy(status = ReservationStatus.CANCELLED))
        notify(reservation.lotteryId, "Reservation cancelled: ${reservation.campsiteName}, " +
                "${reservation.startDate} to ${reservation.endDate}")
    }

    fun reject(reservation: Reservation, reason: String): String?{
        if(reason.isBlank()){
            return "Enter a reason for rejecting"
        }
        if (reservation.status != ReservationStatus.ACTIVE){
            return "Only active reservations can be rejected"
        }

        var campsite: Campsite? = null
        for (site in store.campsites){
            if(site.id == reservation.campsiteId){
                campsite = site
            }
        }

        if(campsite == null || campsite.ownerId != store.uid){
            return "You can only reject reservations at your own campsites"
        }

        store.updateReservation(
            reservation.copy(status = ReservationStatus.CANCELLED, cancelReason = reason.trim())
        )

        store.addNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    lotteryId = reservation.lotteryId,
                    recipientName = reservation.entrantName,
                    recipientId = reservation.entrantId,
                    senderId = store.uid,
                    message = "Your reservation at " + reservation.campsiteName + " (" +
                            reservation.startDate + " to " + reservation.endDate +
                            ") was rejected by the owner. Reason: " + reason.trim(),
                    sentAt = System.currentTimeMillis().toString(),
                    type = NotificationType.CANCELLATION
                )
                )
            return null
    }

    fun rate(reservation: Reservation, stars: Int): String?{
        if (stars < 1 || stars > 5){
            return "Choose 1 to 5 stars"
        }
        if (reservation.entrantId != store.uid){
            return "You can only rate your own stays"
        }
        if(reservation.status == ReservationStatus.CANCELLED){
            return "Cancelled stays cant be rated"
        }
        if (reservation.rating != 0){
            return "You already rated this stay"
        }

        if(reservation.endDate >= today()){
            return "You can rate after your stay ends"
        }

        var campsite: Campsite? = null
        for (site in store.campsites){
            if(site.id == reservation.campsiteId){
                campsite = site
            }
        }
        if (campsite == null){
            return "This campsite no longer exists"
        }

        val newCount = campsite.ratingCount + 1
        val newRating = (campsite.rating * campsite.ratingCount + stars) / newCount

        store.updateCampsite(campsite.copy(rating = newRating, ratingCount = newCount))
        store.updateReservation(reservation.copy(rating = stars))
        return null
    }
    private fun setEntryStatus(lottery: Lottery, status: WaitlistStatus) {
        store.updateLottery(
            lottery.copy(entrants = lottery.entrants.map {
                if (it.entrantId == store.uid) {
                    it.copy(status = status, responseDeadlineAt = null)
                } else it
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
