package com.example.dascamp.controllers

import com.example.dascamp.model.Reservation

object ReservationCsv {
    private fun esc(v: String) =
        if (v.any { it == ',' || it == '"' || it == '\n' }) "\"" + v.replace("\"", "\"\"") + "\"" else v

    fun build(r: Reservation, event: String, at: Long = System.currentTimeMillis()): String {
        val header = "event,reservationId,campsite,entrant,startDate,endDate,guests,status,recordedAt"
        val time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
            .format(java.util.Date(at))
        val row = listOf(
            event, r.id, r.campsiteName, r.entrantName, r.startDate, r.endDate,
            r.guestCount.toString(), r.status.name, time
        ).joinToString(",") { esc(it) }
        return "$header\n$row\n"
    }
}