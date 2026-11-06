package com.example.dascamp.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore

/**
 * All the screens calls this call instead of accessing firestore themselves
 * every list in the app (like campsites, lotteries) is stored as its own collection
 * the listener reacts to the change in firestore and rebuilds the on-screen list for UI to Update
 */
class DasCampRepository(override val uid: String): ObservableModel, CampStore {
    // FIRESTORE COLLECTIONS
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")
    private val lotteriesRef = db.collection("lotteries")
    private val reservationsRef = db.collection("reservations")
    private val notificationsRef = db.collection("notifications")
    private val usersRef = db.collection("users")

    // LIVE LISTS (filled from Firestore, read-only from the outside)
    private val _campsites = mutableStateListOf<Campsite>()
    val campsites: List<Campsite> get() = _campsites

    private val _lotteries = mutableStateListOf<Lottery>()
    val lotteries: List<Lottery> get() = _lotteries

    private val _reservations = mutableStateListOf<Reservation>()
    val reservations: List<Reservation> get() = _reservations

    private val _notifications = mutableStateListOf<Notification>()
    val notifications: List<Notification> get() = _notifications

    private val _users = mutableStateListOf<User>()
    val users: List<User> get() = _users
    val currentUser: User? get() = _users.firstOrNull { it.id == uid } // This device's user, or null if it hasn't signed up (or was deleted)

    // False until the first users snapshot arrives
    // Tells "still loading" apart from "loaded, and this device has no user yet"
    var usersLoaded by mutableStateOf(false)
        private set

    // OPEN ITEMS (which campsites/lottery the user tapped)
    // TODO: this is screen state, not data. Move it into the route arguments later.
    var openCampsiteId: String = ""
    var openLotteryId: String = ""

    // OBSERVER PATTERN
    private val views = mutableListOf<ModelView>()

    override fun addView(view: ModelView) {
        views.add(view)
    }

    override fun removeView(view: ModelView) {
        views.remove(view)
    }

    override fun notifyViews() {
        for (view in views) {
            view.update()
        }
    }

    init {
        listen(campsitesRef, _campsites, Campsite::class.java)
        listen(lotteriesRef, _lotteries, Lottery::class.java)
        listen(reservationsRef, _reservations, Reservation::class.java)
        listen(notificationsRef, _notifications, Notification::class.java)
        listen(usersRef, _users, User::class.java) { usersLoaded = true }
    }

    // CAMPSITES
    fun addCampsite(campsite: Campsite) = save(campsitesRef, campsite.id, campsite)
    fun updateCampsite(campsite: Campsite) = save(campsitesRef, campsite.id, campsite)
    fun deleteCampsite(campsite: Campsite) = remove(campsitesRef, campsite.id)

    // LOTTERIES
    fun addLottery(lottery: Lottery) = save(lotteriesRef, lottery.id, lottery)
    fun updateLottery(lottery: Lottery) = save(lotteriesRef, lottery.id, lottery)
    fun deleteLottery(lottery: Lottery) = remove(lotteriesRef, lottery.id)

    // RESERVATIONS
    fun addReservation(reservation: Reservation) = save(reservationsRef, reservation.id, reservation)
    fun updateReservation(reservation: Reservation) = save(reservationsRef, reservation.id, reservation)
    fun deleteReservation(reservation: Reservation) = remove(reservationsRef, reservation.id)

    // NOTIFICATIONS
    fun addNotification(notification: Notification) = save(notificationsRef, notification.id, notification)
    fun deleteNotification(notification: Notification) = remove(notificationsRef, notification.id)

    // USERS
    fun userById(id: String): User? = _users.firstOrNull { it.id == id }
    fun addUser(user: User) = save(usersRef, user.id, user)
    fun updateUser(user: User) = save(usersRef, user.id, user)
    fun deleteUser(user: User) = remove(usersRef, user.id)

    // HELPERS
    /**
     * Watches one collection
     * On every change, clear the local list and fill it from the documents
     * Clearing first keeps the screen from showing duplicates
     * A failed listen is logged and the old list is left as it was
     */
    private fun <T : Any> listen(
        ref: CollectionReference,
        list: SnapshotStateList<T>,
        type: Class<T>,
        onLoaded: () -> Unit = {}
    ) {
        ref.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e("Firestore", "Listen failed", error)
                return@addSnapshotListener
            }

            list.clear()
            for (document in snapshot.documents) {
                val item = document.toObject(type)
                if (item != null) list.add(item)
            }

            // 
            onLoaded()
            notifyViews()
        }
    }

    // Stores the object as one document. The id is the document name
    private fun <T : Any> save(ref: CollectionReference, id: String, item: T) {
        ref.document(id).set(item)
    }

    // Removes the document that is referenced. Used by every delete method above
    private fun remove(ref: CollectionReference, id: String) {
        ref.document(id).delete()
    }

    //the unsaved campsite data -> save data while picking a location
    var campDraft: CampsiteFormState? = null
}