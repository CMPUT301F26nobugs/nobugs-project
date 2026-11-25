package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Notification
import com.example.dascamp.model.NotificationType
import com.example.dascamp.model.WaitlistStatus
import java.util.UUID

enum class LotteryNotificationAudience(val label: String) {
    ALL_WAITLIST("All Waitlist"),
    WINNERS("Winners"),
    NOT_SELECTED("Not Selected")
}

data class NotificationSendResult(
    val sentCount: Int = 0,
    val error: String? = null
)

/** Coordinates notification inbox actions and lottery outcome messages. */
class NotificationsController(private val store: CampStore) {

    /** Sends one in-app notification record to each matching lottery entrant. */
    fun sendLotteryMessage(
        lotteryId: String,
        audience: LotteryNotificationAudience,
        message: String
    ): NotificationSendResult {
        val owner = store.currentUser
            ?: return NotificationSendResult(error = "Sign up first")
        if (!owner.owner) {
            return NotificationSendResult(error = "Only campsite owners can send lottery notifications")
        }

        val lottery = store.lotteries.firstOrNull { it.id == lotteryId }
            ?: return NotificationSendResult(error = "Lottery not found")

        val ownsLottery = lottery.campsiteIds.isNotEmpty() &&
            lottery.campsiteIds.all { campsiteId ->
                store.campsites.any {
                    it.id == campsiteId && it.ownerId == owner.id
                }
            }
        if (!ownsLottery) {
            return NotificationSendResult(error = "You can only message entrants for your own lottery")
        }

        val body = message.trim()
        if (body.isEmpty()) {
            return NotificationSendResult(error = "Enter a message")
        }

        val recipients = lottery.entrants
            .filter { entrant ->
                when (audience) {
                    LotteryNotificationAudience.ALL_WAITLIST ->
                        entrant.status == WaitlistStatus.WAITING
                    LotteryNotificationAudience.WINNERS ->
                        entrant.status == WaitlistStatus.SELECTED ||
                            entrant.status == WaitlistStatus.BOOKED
                    LotteryNotificationAudience.NOT_SELECTED ->
                        entrant.status == WaitlistStatus.NOT_SELECTED
                }
            }
            .filter { it.entrantId.isNotBlank() }
            .distinctBy { it.entrantId }

        if (recipients.isEmpty()) {
            return NotificationSendResult(error = "No entrants match this recipient group")
        }

        val batchId = UUID.randomUUID().toString()
        val sentAt = System.currentTimeMillis().toString()
        recipients.forEach { entrant ->
            store.addNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    batchId = batchId,
                    lotteryId = lottery.id,
                    recipientName = entrant.name,
                    recipientId = entrant.entrantId,
                    senderId = owner.id,
                    message = body,
                    sentAt = sentAt,
                    type = NotificationType.OWNER_MESSAGE
                )
            )
        }
        return NotificationSendResult(sentCount = recipients.size)
    }

    /** Notifications addressed to the signed-in entrant, newest first. */
    fun inbox(notifications: List<Notification> = store.notifications): List<Notification> =
        notifications
            .filter { it.recipientId == store.uid }
            .sortedByDescending { it.sentAt.toLongOrNull() ?: 0L }

    /** Remove only a notification belonging to the signed-in user. */
    fun dismiss(notification: Notification) {
        if (notification.recipientId == store.uid) {
            store.deleteNotification(notification)
        }
    }

    companion object {
        /** Build the in-app notification saved with a lottery draw result. */
        fun lotteryOutcome(
            lotteryId: String,
            lotteryName: String,
            entrantId: String,
            entrantName: String,
            selected: Boolean,
            sentAt: String = System.currentTimeMillis().toString()
        ): Notification {
            val type = if (selected) NotificationType.WIN else NotificationType.LOSE
            return Notification(
                id = java.util.UUID.randomUUID().toString(),
                lotteryId = lotteryId,
                recipientName = entrantName,
                recipientId = entrantId,
                message = if (selected) {
                    "You won the lottery: $lotteryName"
                } else {
                    "You were not selected for: $lotteryName"
                },
                sentAt = sentAt,
                type = type
            )
        }
    }
}
