package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
        endDate: String = "",
        minimumStayDays: Int = 1
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

            // Match if the search window contains a continuous, bookable run
            // at least as long as the requested inclusive minimum stay.
            val hasDateWindow = startDate.isNotBlank() && endDate.isNotBlank()
            val matchesDates = !hasDateWindow || (
                    minimumStayDays >= 1 &&
                            startDate <= endDate &&
                            hasBookableRun(
                                campsite = campsite,
                                windowStart = startDate,
                                windowEnd = endDate,
                                minimumStayDays = minimumStayDays,
                                reservations = reservations
                            )
                    )

            matchesQuery && matchesLocation && matchesGuests && matchesDates
        }
    }

    private fun hasBookableRun(
        campsite: Campsite,
        windowStart: String,
        windowEnd: String,
        minimumStayDays: Int,
        reservations: List<Reservation>
    ): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun toEpochDay(value: String): Long? =
            runCatching {
                formatter.parse(value)?.time?.div(MILLIS_PER_DAY)
            }.getOrNull()

        val firstDay = toEpochDay(windowStart) ?: return false
        val lastDay = toEpochDay(windowEnd) ?: return false
        if (lastDay < firstDay) return false

        var consecutiveAvailableDays = 0
        for (epochDay in firstDay..lastDay) {
            val date = formatter.format(Date(epochDay * MILLIS_PER_DAY))
            val ownerAllowsDate = campsite.availability.any {
                it.start <= date && date <= it.end
            }
            val dateIsBooked = reservations.any {
                it.campsiteId == campsite.id &&
                    it.status == ReservationStatus.ACTIVE &&
                    it.startDate <= date &&
                    date <= it.endDate
            }

            if (ownerAllowsDate && !dateIsBooked) {
                consecutiveAvailableDays++
                if (consecutiveAvailableDays >= minimumStayDays) return true
            } else {
                consecutiveAvailableDays = 0
            }
        }
        return false
    }

    private companion object {
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
