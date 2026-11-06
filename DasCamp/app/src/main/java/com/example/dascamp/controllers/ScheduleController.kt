package com.example.dascamp.controllers

import com.example.dascamp.model.Campsite
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import java.util.UUID

/**
 * SCHEDULE CONTROLLER
 *
 * Responsibilities:
 * - Find dates that are already booked
 * - Check if dates are still available
 * - Check if date is inside owner's availability
 *
 * Collaborators:
 * - DasCampRepository, Campsite, Reservation, DateRange
 *
 * Stories: US 02.06.01, US 01.04.01
 */
class ScheduleController (private val repository: DasCampRepository){
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
        val booked = bookedReservations(campsiteId, repository.reservations)
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
        val booked = bookedReservations(campsite.id, repository.reservations)
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

        //inside lotterys boooking period
        if(lottery != null){
            if(startDate < lottery.bookingStart || endDate> lottery.bookingEnd) {
                return "Dates between " + lottery.bookingStart + " and " + lottery.bookingEnd
            }
        }

        //guest in occupancy allowed
        if(guestCount < 1 || guestCount >  campsite.maxOccupancy){
            return "Guest must be between 1 " + campsite.maxOccupancy.toString()
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
            lotteryId = lottery?.id,
            guestCount = guestCount,
            rulesAccepted = rulesAccepted
        )
        repository.addReservation(reservation)

        // mark this entrants as booked in lottery
        return null
    }
}