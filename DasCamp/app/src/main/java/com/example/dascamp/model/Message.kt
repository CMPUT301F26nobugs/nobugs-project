package com.example.dascamp.model

/**
 *
 */
data class OwnerMessage(
    val id: String = "",
    val reservationId: String = "",
    val entrantName: String = "",
    val body: String = "",
    val sentAt: String = "",
    val senderId: String = "",
    val receiverId: String = ""
)
