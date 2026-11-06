package com.example.dascamp.controllers

import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus

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

}