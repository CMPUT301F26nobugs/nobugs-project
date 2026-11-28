package com.example.dascamp.model

import java.util.UUID

/** Lifecycle of a multi-owner lottery request. */
enum class LotteryProposalStatus {
    PENDING, PUBLISHED, REJECTED, EXPIRED, CANCELLED
}

/** One campsite owner's decision for every campsite they contribute. */
enum class OwnerApprovalStatus {
    PENDING, APPROVED, DECLINED
}

/** Result of attempting a transactional proposal action. */
enum class LotteryProposalAction {
    UPDATED,
    NOT_FOUND,
    NOT_PENDING,
    NOT_AUTHORIZED,
    ALREADY_RESPONDED,
    LOTTERY_ALREADY_EXISTS
}

data class LotteryOwnerApproval(
    val ownerId: String = "",
    val ownerName: String = "",
    val campsiteIds: List<String> = emptyList(),
    val status: OwnerApprovalStatus = OwnerApprovalStatus.PENDING
)

/**
 * A proposed shared lottery remains here until all distinct campsite owners
 * approve it. Its draft is copied into the public lotteries collection only
 * when the proposal is published.
 */
data class LotteryProposal(
    val id: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val lotteryDraft: Lottery = Lottery(),
    val ownerApprovals: List<LotteryOwnerApproval> = emptyList(),
    /** Epoch milliseconds at the end of the selected approval-deadline date. */
    val approvalDeadlineAt: Long = 0L,
    val createdAt: Long = 0L,
    val status: LotteryProposalStatus = LotteryProposalStatus.PENDING
) {
    /** Creates one notification record per distinct selected recipient. */
    fun notifications(
        type: NotificationType,
        message: String,
        senderId: String,
        recipientIds: List<String> = ownerApprovals.map { it.ownerId }
    ): List<Notification> {
        val ownersById = ownerApprovals.associateBy { it.ownerId }
        val sentAt = System.currentTimeMillis().toString()
        return recipientIds.distinct().mapNotNull { recipientId ->
            val owner = ownersById[recipientId] ?: return@mapNotNull null
            Notification(
                id = UUID.randomUUID().toString(),
                lotteryId = lotteryDraft.id,
                proposalId = id,
                recipientName = owner.ownerName,
                recipientId = owner.ownerId,
                senderId = senderId,
                message = message,
                sentAt = sentAt,
                type = type
            )
        }
    }
}
