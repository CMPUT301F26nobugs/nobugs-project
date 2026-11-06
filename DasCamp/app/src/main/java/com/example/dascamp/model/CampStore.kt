package com.example.dascamp.model

/**
 * Data access used by controllers.
 */
interface CampStore {
    val uid: String
    val campsites: List<Campsite>
    val lotteries: List<Lottery>
    val reservations: List<Reservation>
    val notifications: List<Notification>
    val users: List<User>
    val currentUser: User?

    fun addCampsite(campsite: Campsite)
    fun updateCampsite(campsite: Campsite)
    fun deleteCampsite(campsite: Campsite)
    fun addLottery(lottery: Lottery)
    fun updateLottery(lottery: Lottery)
    fun deleteLottery(lottery: Lottery)
    fun addReservation(reservation: Reservation)
    fun updateReservation(reservation: Reservation)
    fun deleteReservation(reservation: Reservation)
    fun addNotification(notification: Notification)
    fun deleteNotification(notification: Notification)
    fun userById(id: String): User?
    fun addUser(user: User)
    fun updateUser(user: User)
    fun deleteUser(user: User)
}