package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Notification
import com.example.dascamp.model.NotificationType

/** Coordinates notification inbox actions and lottery outcome messages. */
class NotificationsController(private val store: CampStore) {

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
