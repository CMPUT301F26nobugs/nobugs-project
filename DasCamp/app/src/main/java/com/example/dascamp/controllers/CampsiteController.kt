package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.ReservationStatus

/**
 * CAMPSITE CONTROLLER
 *
 * Responsibilities:
 * - Filter campsites by search text, location, guest count, and date
 * - Hide campsites whose requested dates are already reserved
 *
 * Collaborators:
 * - DasCampstore (campsites and reservations)
 * - Entrant campsite list screen
 *
 * Stories: US 01.01.01, 01.01.02, 01.01.03, 01.01.04
 */
class CampsiteController(private val store: CampStore) {

    // Filters the store's campsites for the entrant search. Blank or null filters are ignored
    fun searchCampsites(
        query: String = "",
        location: String = "",
        guests: Int? = null,
        startDate: String = "",
        endDate: String = ""
    ): List<Campsite> {
        val reservations = store.reservations
        val q = query.trim()

        return store.campsites.filter { campsite ->

            // Search text matches the name, location, or any tag
            val matchesQuery = q.isEmpty() ||
                    campsite.name.contains(q, ignoreCase = true) ||
                    campsite.location.contains(q, ignoreCase = true) ||
                    campsite.tags.any { it.contains(q, ignoreCase = true) }

            // Location filter
            val matchesLocation = location.isBlank() ||
                    campsite.location.contains(location.trim(), ignoreCase = true)

            // The campsite must fit the number of guests
            val matchesGuests = guests == null || campsite.maxOccupancy >= guests

            // Dates are yyyy-MM-dd, so comparing them as strings orders them correctly
            // The range must sit inside an availability range and not overlap an active reservation
            val matchesDates = startDate.isBlank() || endDate.isBlank() ||
                    (campsite.availability.any { it.start <= startDate && endDate <= it.end } &&
                            reservations.none {
                                it.campsiteId == campsite.id &&
                                        it.status == ReservationStatus.ACTIVE &&
                                        it.startDate <= endDate && startDate <= it.endDate
                            })

            matchesQuery && matchesLocation && matchesGuests && matchesDates
        }
    }
}