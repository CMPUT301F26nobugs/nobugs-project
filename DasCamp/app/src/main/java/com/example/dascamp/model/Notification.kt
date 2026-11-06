package com.example.dascamp.model

/**
 * The kind of event a [Notification] is announcing.
 *
 * - [OWNER_MESSAGE]: a direct message from a campsite owner to an entrant.
 * - [WIN]: the entrant was selected in a lottery.
 * - [LOSE]: the entrant was not selected in a lottery.
 * - [CONFIRMATION]: a reservation or selection was confirmed.
 * - [CANCELLATION]: a reservation or selection was cancelled.
 */
enum class NotificationType { OWNER_MESSAGE, WIN, LOSE, CONFIRMATION, CANCELLATION }

/**
 * A notification sent to a user, such as a lottery result or a message from a campsite owner.
 *
 * All properties have defaults so the class has a no-arg constructor,
 * which is needed for deserialization (e.g. Firestore's `toObject()`).
 *
 * @property id unique identifier of the notification
 * @property lotteryId id of the related lottery, or null if the notification isn't tied to one
 * (e.g. a plain [NotificationType.OWNER_MESSAGE])
 * @property recipientName display name of the user receiving the notification
 * @property message the text content shown to the recipient
 * @property sentAt when the notification was sent, as a string (state the format, e.g. ISO-8601)
 * @property senderId id of the user or system that sent the notification
 * @property recipientId id of the user the notification is addressed to
 * @property type what kind of event this notification represents
 */
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

