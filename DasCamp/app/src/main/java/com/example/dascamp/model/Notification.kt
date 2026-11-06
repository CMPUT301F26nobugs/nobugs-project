package com.example.dascamp.model

enum class NotificationType { OWNER_MESSAGE, WIN, LOSE, CONFIRMATION, CANCELLATION }

data class Notification(
    val id: String = "",
    val lotteryId: String? = null,
    val recipientName: String = "",
    val message: String = "",
    val sentAt: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val type: NotificationType = NotificationType.OWNER_MESSAGE
)

