package com.example.dascamp

import com.example.dascamp.model.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

/**
 * In-memory [CampStore] for JUnit tests.
 *
 * each test gets a clean store with no Firestore.
 */
class FakeCampStore(override val uid: String) : CampStore {
    private val _campsites = mutableListOf<Campsite>()
    private val _lotteries = mutableListOf<Lottery>()
    private val _lotteryProposals = mutableListOf<LotteryProposal>()
    private val _reservations = mutableListOf<Reservation>()
    private val _notifications = mutableListOf<Notification>()
    private val _users = mutableListOf<User>()
    private val _messages = mutableListOf<OwnerMessage>()
    private val _reviews = mutableListOf<CampsiteReview>()

    override val campsites: List<Campsite> get() = _campsites
    override val lotteries: List<Lottery> get() = _lotteries
    override val lotteryProposals: List<LotteryProposal> get() = _lotteryProposals
    override val reservations: List<Reservation> get() = _reservations
    override val notifications: List<Notification> get() = _notifications
    override val users: List<User> get() = _users
    override val messages: List<OwnerMessage> get() = _messages
    override val reviews: List<CampsiteReview> get() = _reviews
    override val currentUser: User? get() = _users.firstOrNull { it.id == uid }

    override fun addCampsite(campsite: Campsite) {
        _campsites.removeAll { it.id == campsite.id }
        _campsites.add(campsite)
    }
    override fun updateCampsite(campsite: Campsite) = addCampsite(campsite)
    override fun deleteCampsite(campsite: Campsite) {
        _campsites.removeAll { it.id == campsite.id }
    }

    override fun addLottery(lottery: Lottery) {
        _lotteries.removeAll { it.id == lottery.id }
        _lotteries.add(lottery)
    }
    override fun updateLottery(lottery: Lottery) = addLottery(lottery)
    override fun deleteLottery(lottery: Lottery) {
        _lotteries.removeAll { it.id == lottery.id }
    }

    override fun createLotteryProposal(
        proposal: LotteryProposal,
        requestNotifications: List<Notification>
    ): Task<Void> {
        _lotteryProposals.removeAll { it.id == proposal.id }
        _lotteryProposals.add(proposal)
        requestNotifications.forEach(::addNotification)
        return Tasks.forResult<Void>(null)
    }

    override fun respondToLotteryProposal(
        proposalId: String,
        ownerId: String,
        approve: Boolean
    ): Task<LotteryProposalAction> {
        val proposal = _lotteryProposals.firstOrNull { it.id == proposalId }
            ?: return Tasks.forResult(LotteryProposalAction.NOT_FOUND)
        if (proposal.status != LotteryProposalStatus.PENDING) {
            return Tasks.forResult(LotteryProposalAction.NOT_PENDING)
        }
        val ownApproval = proposal.ownerApprovals.firstOrNull { it.ownerId == ownerId }
            ?: return Tasks.forResult(LotteryProposalAction.NOT_AUTHORIZED)
        if (ownApproval.status != OwnerApprovalStatus.PENDING) {
            return Tasks.forResult(LotteryProposalAction.ALREADY_RESPONDED)
        }

        val approvals = proposal.ownerApprovals.map { approval ->
            if (approval.ownerId == ownerId) {
                approval.copy(
                    status = if (approve) OwnerApprovalStatus.APPROVED
                    else OwnerApprovalStatus.DECLINED
                )
            } else approval
        }
        val rejected = !approve
        val allApproved = approvals.all { it.status == OwnerApprovalStatus.APPROVED }
        val updated = proposal.copy(
            ownerApprovals = approvals,
            status = when {
                rejected -> LotteryProposalStatus.REJECTED
                allApproved -> LotteryProposalStatus.PUBLISHED
                else -> LotteryProposalStatus.PENDING
            }
        )
        _lotteryProposals.removeAll { it.id == proposalId }
        _lotteryProposals.add(updated)
        when {
            rejected -> updated.notifications(
                NotificationType.LOTTERY_PROPOSAL_UPDATE,
                "The shared lottery proposal \"${proposal.lotteryDraft.name}\" was rejected.",
                proposal.creatorId
            ).forEach(::addNotification)
            allApproved -> {
                addLottery(proposal.lotteryDraft.copy(status = LotteryStatus.OPEN))
                updated.notifications(
                    NotificationType.LOTTERY_PROPOSAL_UPDATE,
                    "The shared lottery \"${proposal.lotteryDraft.name}\" was published.",
                    proposal.creatorId
                ).forEach(::addNotification)
            }
        }
        return Tasks.forResult(LotteryProposalAction.UPDATED)
    }

    override fun cancelLotteryProposal(
        proposalId: String,
        creatorId: String
    ): Task<LotteryProposalAction> {
        val proposal = _lotteryProposals.firstOrNull { it.id == proposalId }
            ?: return Tasks.forResult(LotteryProposalAction.NOT_FOUND)
        if (proposal.creatorId != creatorId) {
            return Tasks.forResult(LotteryProposalAction.NOT_AUTHORIZED)
        }
        if (proposal.status != LotteryProposalStatus.PENDING) {
            return Tasks.forResult(LotteryProposalAction.NOT_PENDING)
        }
        val cancelled = proposal.copy(status = LotteryProposalStatus.CANCELLED)
        _lotteryProposals.removeAll { it.id == proposalId }
        _lotteryProposals.add(cancelled)
        cancelled.notifications(
            NotificationType.LOTTERY_PROPOSAL_UPDATE,
            "The shared lottery proposal \"${proposal.lotteryDraft.name}\" was cancelled.",
            creatorId
        ).forEach(::addNotification)
        return Tasks.forResult(LotteryProposalAction.UPDATED)
    }

    override fun addReservation(reservation: Reservation) {
        _reservations.removeAll { it.id == reservation.id }
        _reservations.add(reservation)
    }
    override fun updateReservation(reservation: Reservation) = addReservation(reservation)
    override fun deleteReservation(reservation: Reservation) {
        _reservations.removeAll { it.id == reservation.id }
    }

    override fun addNotification(notification: Notification) {
        _notifications.removeAll { it.id == notification.id }
        _notifications.add(notification)
    }
    override fun deleteNotification(notification: Notification) {
        _notifications.removeAll { it.id == notification.id }
    }

    override fun userById(id: String): User? = _users.firstOrNull { it.id == id }
    override fun addUser(user: User) {
        _users.removeAll { it.id == user.id }
        _users.add(user)
    }
    override fun updateUser(user: User) = addUser(user)
    override fun deleteUser(user: User) {
        _users.removeAll { it.id == user.id }
    }

    override fun addMessage(message: OwnerMessage) {
        _messages.removeAll { it.id == message.id }
        _messages.add(message)
    }
    override fun updateMessage(message: OwnerMessage) = addMessage(message)
    override fun deleteMessage(message: OwnerMessage) {
        _messages.removeAll { it.id == message.id }
    }

    override fun addReview(review: CampsiteReview) {
        _reviews.removeAll { it.id == review.id }
        _reviews.add(review)
    }
    override fun updateReview(review: CampsiteReview) = addReview(review)
    override fun deleteReview(review: CampsiteReview) {
        _reviews.removeAll { it.id == review.id }
    }
}
