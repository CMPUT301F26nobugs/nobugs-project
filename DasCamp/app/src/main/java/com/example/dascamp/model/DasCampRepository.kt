package com.example.dascamp.model

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore

/**
 * All the screens calls this call instead of accessing firestore themselves
 * every list in the app (like campsites, lotteries) is stored as its own collection
 * the listener reacts to the change in firestore and rebuilds the on-screen list for UI to Update
 */
class DasCampRepository {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")
    private val lotteriesRef = db.collection("lotteries")
    private val reservationsRef = db.collection("reservations")
    private val notificationsRef = db.collection("notifications")
    private val messagesRef = db.collection("messages")
    private val ownersRef = db.collection("owners")
    private val entrantsRef = db.collection("entrants")
    private val _campsites = mutableStateListOf<Campsite>()
    val campsites: List<Campsite> get() = _campsites
    private val _lotteries = mutableStateListOf<Lottery>()
    val lotteries: List<Lottery> get() = _lotteries
    private val _reservations = mutableStateListOf<Reservation>()
    val reservations: List<Reservation> get() = _reservations
    private val _notifications = mutableStateListOf<OwnerNotification>()
    val notifications: List<OwnerNotification> get() = _notifications
    private val _messages = mutableStateListOf<OwnerMessage>()
    val messages: List<OwnerMessage> get() = _messages
    private val _owners = mutableStateListOf<OwnerProfile>()
    val owners: List<OwnerProfile> get() = _owners
    private val _entrants = mutableStateListOf<EntrantProfile>()
    val entrants: List<EntrantProfile> get() = _entrants
    init {
        listen(campsitesRef, _campsites, Campsite::class.java)
        listen(lotteriesRef, _lotteries, Lottery::class.java)
        listen(reservationsRef, _reservations, Reservation::class.java)
        listen(notificationsRef, _notifications, OwnerNotification::class.java)
        listen(messagesRef, _messages, OwnerMessage::class.java)
        listen(ownersRef, _owners, OwnerProfile::class.java)
        listen(entrantsRef, _entrants, EntrantProfile::class.java)
    }
    fun addCampsite(campsite: Campsite) = save(campsitesRef, campsite.id, campsite)
    fun updateCampsite(campsite: Campsite) = save(campsitesRef, campsite.id, campsite)
    fun deleteCampsite(campsite: Campsite) = remove(campsitesRef, campsite.id)
    fun addLottery(lottery: Lottery) = save(lotteriesRef, lottery.id, lottery)
    fun updateLottery(lottery: Lottery) = save(lotteriesRef, lottery.id, lottery)
    fun deleteLottery(lottery: Lottery) = remove(lotteriesRef, lottery.id)
    fun addReservation(reservation: Reservation) = save(reservationsRef, reservation.id, reservation)
    fun updateReservation(reservation: Reservation) = save(reservationsRef, reservation.id, reservation)
    fun deleteReservation(reservation: Reservation) = remove(reservationsRef, reservation.id)
    fun addNotification(notification: OwnerNotification) = save(notificationsRef, notification.id, notification)
    fun deleteNotification(notification: OwnerNotification) = remove(notificationsRef, notification.id)
    fun addMessage(message: OwnerMessage) = save(messagesRef, message.id, message)
    fun deleteMessage(message: OwnerMessage) = remove(messagesRef, message.id)
    fun addOwner(owner: OwnerProfile) = save(ownersRef, owner.id, owner)
    fun updateOwner(owner: OwnerProfile) = save(ownersRef, owner.id, owner)
    fun deleteOwner(owner: OwnerProfile) = remove(ownersRef, owner.id)
    fun addEntrant(entrant: EntrantProfile) = save(entrantsRef, entrant.id, entrant)
    fun updateEntrant(entrant: EntrantProfile) = save(entrantsRef, entrant.id, entrant)
    fun deleteEntrant(entrant: EntrantProfile) = remove(entrantsRef, entrant.id)

    /**
     * Watches one collection
     * On every change, clear the local list and fill it from the documents
     * Clearing first keeps the screen from showing duplicates
     * A failed listen is logged and the old list is left as it was
     */
    private fun <T : Any> listen(
        ref: CollectionReference,
        list: SnapshotStateList<T>,
        type: Class<T>
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
}