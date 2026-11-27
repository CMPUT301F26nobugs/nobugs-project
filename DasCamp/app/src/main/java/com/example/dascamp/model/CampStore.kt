package com.example.dascamp.model

import com.google.android.gms.tasks.Task

/**
 * Data access used by controllers.
 */
interface CampStore {
    val uid: String
    val campsites: List<Campsite>
    val lotteries: List<Lottery>
    val lotteryProposals: List<LotteryProposal>
    val reservations: List<Reservation>
    val notifications: List<Notification>
    val users: List<User>
    val messages: List<OwnerMessage>
    val reviews: List<CampsiteReview>
    val currentUser: User?

    fun addCampsite(campsite: Campsite)
    fun updateCampsite(campsite: Campsite)
    fun deleteCampsite(campsite: Campsite)
    fun addLottery(lottery: Lottery)
    fun updateLottery(lottery: Lottery)
    fun deleteLottery(lottery: Lottery)
    fun createLotteryProposal(
        proposal: LotteryProposal,
        requestNotifications: List<Notification>
    ): Task<Void>
    fun respondToLotteryProposal(
        proposalId: String,
        ownerId: String,
        approve: Boolean
    ): Task<LotteryProposalAction>
    fun cancelLotteryProposal(
        proposalId: String,
        creatorId: String
    ): Task<LotteryProposalAction>
    fun addReservation(reservation: Reservation)
    fun updateReservation(reservation: Reservation)
    fun deleteReservation(reservation: Reservation)
    fun addNotification(notification: Notification)
    fun deleteNotification(notification: Notification)
    fun userById(id: String): User?
    fun addUser(user: User)
    fun updateUser(user: User)
    fun deleteUser(user: User)
    fun addMessage(message: OwnerMessage)
    fun updateMessage(message: OwnerMessage)
    fun deleteMessage(message: OwnerMessage)
    fun addReview(review: CampsiteReview)
    fun updateReview(review: CampsiteReview)
    fun deleteReview(review: CampsiteReview)
}
