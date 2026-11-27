package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.Notification
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.WaitlistStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * SCHEDULE CONTROLLER (Availability Schedule feature)
 *
 * Responsibilities:
 * - Find dates that are already booked
 * - Check if dates are still available
 * - Check if a date is inside the owner's availability
 * - Reserve dates for a lottery winner, first come first served
 *
 * Collaborators:
 * - CampStore, Campsite, Reservation, Lottery, DateRange
 *
 * All dates are Strings in "yyyy-MM-dd", so they can be compared with < and >.
 *
 * Stories: US 02.06.01, 01.01.05, 01.04.01, 02.11.01, 02.04.04
 */
class ScheduleController (private val store: CampStore){
    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    //active reservations for campsite
    fun bookedReservations(campsiteId: String, reservations: List<Reservation>): List<Reservation>{
        val booked = mutableListOf<Reservation>()
        for (reservation in reservations){
            if (reservation.campsiteId == campsiteId && reservation.status == ReservationStatus.ACTIVE){
                booked.add(reservation)
            }
        }
        return booked
    }

    // Checks if date is available within owners availability
    fun isInAvailability(campsite: Campsite, date: String): Boolean{
        for (range in campsite.availability){
            if(date >= range.start && date <= range.end){
                return true
            }
        }
        return false
    }

    // checks if an active reservation has this date
    fun isDateBooked(campsiteId: String, date: String): Boolean{
        val booked = bookedReservations(campsiteId, store.reservations)
        for (reservation in booked){
            if(date >= reservation.startDate && date <= reservation.endDate){
                return true
            }
        }
        return false
    }

    // checks if entrant can pick this date
    fun isDateAvailable(campsite: Campsite, date: String): Boolean{
        if (!isInAvailability(campsite, date)){
            return false
        }
        if (isDateBooked(campsite.id, date)){
            return false
        }
        return true
    }

    //Checks if whole range can be booked
    fun isRangeAvailable(campsite: Campsite, startDate: String, endDate: String): Boolean{
        var fits = false
        for (range in campsite.availability){
            if(startDate >= range.start && endDate <= range.end){
                fits = true
            }
        }
        if (!fits){
            return false
        }
        //check if overlaps
        val booked = bookedReservations(campsite.id, store.reservations)
        for (reservation in booked){
            if(startDate <= reservation.endDate && endDate >= reservation.startDate){
                return false
            }
        }
        return true
    }

    fun reserveDates(
        campsite: Campsite,
        lottery: Lottery?,
        entrantId: String,
        entrantName: String,
        startDate: String,
        endDate: String,
        guestCount: Int,
        rulesAccepted: Boolean
    ): String?{

        if (entrantId != store.uid) {
            return "Only the signed-in entrant can book this lottery spot"
        }
        // A campsite reservation is only available to a selected lottery entrant.
        if (lottery == null || lottery.status != LotteryStatus.DRAWN) {
            return "You can book only after being selected in an active lottery"
        }
        if (campsite.id !in lottery.campsiteIds) {
            return "Choose a campsite included in this lottery"
        }
        val entry = lottery.entrants.firstOrNull { it.entrantId == entrantId }
            ?: return "You are not on this lottery waitlist"
        if (entry.status != WaitlistStatus.SELECTED) {
            return "Only selected entrants can book this campsite"
        }
        if (today() > lottery.bookingEnd) return "The booking period has ended"
        if (entry.responseDeadlineAt?.let { System.currentTimeMillis() >= it } == true) {
            return "Your response period has ended"
        }

        //Entrant must accept rules
        if(!rulesAccepted){
            return "Confirm You have read the rules and policies"
        }

        //Dates have a format yyyy-mm-dd
        if(startDate.length != 10 || endDate.length != 10){
            return "Use format: yyyy-mm-dd"
        }

        //end cant be before start
        if(endDate < startDate){
            return "The end date has to be on or after start date"
        }

        // Requested dates must fit inside the winner's booking window.
        if(startDate < lottery.bookingStart || endDate > lottery.bookingEnd) {
            return "Dates between " + lottery.bookingStart + " and " + lottery.bookingEnd
        }

        //guest in occupancy allowed
        if(guestCount < 1 || guestCount >  campsite.maxOccupancy){
            return "Guest must be between 1 and " + campsite.maxOccupancy.toString()
        }

        // are dates still free (first come first served)
        if(!isRangeAvailable(campsite, startDate, endDate)){
            return "Unfortunately those dates are not available "
        }

        val reservation = Reservation(
            id = UUID.randomUUID().toString(),
            campsiteId = campsite.id,
            campsiteName = campsite.name,
            entrantId = entrantId,
            entrantName = entrantName,
            startDate = startDate,
            endDate = endDate,
            status = ReservationStatus.ACTIVE,
            lotteryId = lottery.id,
            guestCount = guestCount,
            rulesAccepted = rulesAccepted
        )
        store.addReservation(reservation) // add reservation to firebase
        store.addNotification( // add notification to firebase
            Notification(
                id = UUID.randomUUID().toString(),
                lotteryId = lottery.id,
                recipientName = entrantName,
                recipientId = entrantId,
                message = "Reservation confirmed: ${campsite.name}, $startDate to $endDate",
                sentAt = System.currentTimeMillis().toString()
            )
        )


        store.updateLottery(lottery.copy(entrants = lottery.entrants.map {
            if (it.entrantId == entrantId) {
                it.copy(status = WaitlistStatus.BOOKED, responseDeadlineAt = null)
            } else it
        }))
        return null
    }
}
