package com.example.dascamp.controllers

import com.example.dascamp.model.Reservation

object CancellationCsv {
    private fun esc(v: String): String =
        if (v.any { it == ',' || it == '"' || it == '\n' }) "\"" + v.replace(
            "\"",
            "\"\""
        ) + "\"" else v

    fun build(r: Reservation, cancelledAt: Long = System.currentTimeMillis()): String {
        val header = "reservationId,campsite,entrant,startDate,endDate,guests,cancelledAt"
        val row = listOf(
            r.id, r.campsiteName, r.entrantName, r.startDate, r.endDate,
            r.guestCount.toString(),
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                .format(java.util.Date(cancelledAt))
        ).joinToString(",") { esc(it) }
        return "$header\n$row\n"
    }
}