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
    private val messagesRef = db.collection("messages")
    private val reviewsRef = db.collection("reviews")

    // LIVE LISTS (filled from Firestore, read-only from the outside)
    private val _campsites = mutableStateListOf<Campsite>()
    override val campsites: List<Campsite> get() = _campsites
    var campsitesLoaded by mutableStateOf(false)
        private set

    private val _lotteries = mutableStateListOf<Lottery>()
    override val lotteries: List<Lottery> get() = _lotteries

    private val _reservations = mutableStateListOf<Reservation>()
    override val reservations: List<Reservation> get() = _reservations

    private val _notifications = mutableStateListOf<Notification>()
    override val notifications: List<Notification> get() = _notifications

    private val _users = mutableStateListOf<User>()
    override val users: List<User> get() = _users

    private val _messages = mutableStateListOf<OwnerMessage>()
    override val messages: List<OwnerMessage> get() = _messages

    private val _reviews = mutableStateListOf<CampsiteReview>()
    override val reviews: List<CampsiteReview> get() = _reviews

    override val currentUser: User? get() = _users.firstOrNull { it.id == uid } // This device's user, or null if it hasn't signed up (or was deleted)

    // False until the first users snapshot arrives
    // Tells "still loading" apart from "loaded, and this device has no user yet"
    var usersLoaded by mutableStateOf(false)
        private set

    // OPEN ITEMS (which campsites/lottery the user tapped)
    // TODO: this is screen state, not data. Move it into the route arguments later.
    var openCampsiteId: String = ""
    var openLotteryId: String = ""
    /** Reservation context for opening an owner/entrant conversation. US 01.14.01 */
    var openReservationId: String = ""
    /** Owner/entrant pair currently open in the shared messaging screen. */
    var openMessageOwnerId: String = ""
    var openMessageEntrantId: String = ""
    var openMessageLotteryId: String? = null

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
        listen(campsitesRef, _campsites, Campsite::class.java) { campsitesLoaded = true }
        listen(lotteriesRef, _lotteries, Lottery::class.java)
        listen(reservationsRef, _reservations, Reservation::class.java)
        listen(notificationsRef, _notifications, Notification::class.java)
        listen(usersRef, _users, User::class.java) { usersLoaded = true }
        listen(messagesRef, _messages, OwnerMessage::class.java)
        listen(reviewsRef, _reviews, CampsiteReview::class.java)
    }

    // CAMPSITES
    override fun addCampsite(campsite: Campsite) = save(campsitesRef, campsite.id, campsite)
    override fun updateCampsite(campsite: Campsite) = save(campsitesRef, campsite.id, campsite)
    override fun deleteCampsite(campsite: Campsite) = remove(campsitesRef, campsite.id)

    // LOTTERIES
    override fun addLottery(lottery: Lottery) = save(lotteriesRef, lottery.id, lottery)
    override fun updateLottery(lottery: Lottery) = save(lotteriesRef, lottery.id, lottery)
    override fun deleteLottery(lottery: Lottery) = remove(lotteriesRef, lottery.id)

    // RESERVATIONS
    override fun addReservation(reservation: Reservation) = save(reservationsRef, reservation.id, reservation)
    override fun updateReservation(reservation: Reservation) = save(reservationsRef, reservation.id, reservation)
    override fun deleteReservation(reservation: Reservation) = remove(reservationsRef, reservation.id)

    // NOTIFICATIONS
    override fun addNotification(notification: Notification) = save(notificationsRef, notification.id, notification)
    override fun deleteNotification(notification: Notification) = remove(notificationsRef, notification.id)

    // USERS
    override fun userById(id: String): User? = _users.firstOrNull { it.id == id }
    override fun addUser(user: User) = save(usersRef, user.id, user)
    override fun updateUser(user: User) = save(usersRef, user.id, user)
    override fun deleteUser(user: User) = remove(usersRef, user.id)

    // MESSAGES (US 01.14.01)
    override fun addMessage(message: OwnerMessage) = save(messagesRef, message.id, message)
    override fun updateMessage(message: OwnerMessage) = save(messagesRef, message.id, message)
    override fun deleteMessage(message: OwnerMessage) = remove(messagesRef, message.id)

    // REVIEWS (US 01.13.02, US 03.09.01, US 03.10.01)
    override fun addReview(review: CampsiteReview) = save(reviewsRef, review.id, review)
    override fun updateReview(review: CampsiteReview) = save(reviewsRef, review.id, review)
    override fun deleteReview(review: CampsiteReview) = remove(reviewsRef, review.id)

    // HELPERS
    /**
     * Watches one collection
     * On every change, clear the local list and fill it from the documents
     * Clearing first keeps the screen from showing duplicates
     * A failed listen is logged and the old list is left as it was
     * Bad documents are logged and skipped
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
                val item = try {
                    document.toObject(type)
                } catch (e: Exception) {
                    Log.e("Firestore", "Skipping bad document ${document.id} in ${ref.path}", e)
                    null
                }
                if (item != null) list.add(item)
            }

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
