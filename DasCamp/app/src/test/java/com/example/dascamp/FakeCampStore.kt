package com.example.dascamp

import com.example.dascamp.model.*

/**
 * In-memory [CampStore] for JUnit tests.
 *
 * each test gets a clean store with no Firestore.
 */
class FakeCampStore(override val uid: String) : CampStore {
    private val _campsites = mutableListOf<Campsite>()
    private val _lotteries = mutableListOf<Lottery>()
    private val _reservations = mutableListOf<Reservation>()
    private val _notifications = mutableListOf<Notification>()
    private val _users = mutableListOf<User>()

    override val campsites: List<Campsite> get() = _campsites
    override val lotteries: List<Lottery> get() = _lotteries
    override val reservations: List<Reservation> get() = _reservations
    override val notifications: List<Notification> get() = _notifications
    override val users: List<User> get() = _users
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
}