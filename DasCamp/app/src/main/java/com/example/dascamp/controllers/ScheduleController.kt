package com.example.dascamp.controllers

import com.example.dascamp.model.Campsite
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import com.google.type.Date

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
}