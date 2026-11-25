package com.example.dascamp

import com.example.dascamp.controllers.AdminController
import com.example.dascamp.controllers.MessageController
import com.example.dascamp.controllers.ProfileController
import com.example.dascamp.controllers.ReviewController
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.LotteryFormState
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Covers the added entrant, owner, and admin stories:
 * profile image, reviews, reservation messages, shared lotteries, and moderation.
 */
class AddedStoriesTest {
    private lateinit var store: FakeCampStore

    @Before
    fun setUp() {
        store = FakeCampStore("entrant-1")
        store.addUser(User(id = "entrant-1", firstName = "Ada", lastName = "Entrant", entrant = true))
        store.addUser(User(id = "owner-1", firstName = "Owen", lastName = "Owner", owner = true))
        store.addUser(User(id = "owner-2", firstName = "Pat", lastName = "Partner", owner = true))
        store.addCampsite(site("camp-1", "owner-1", "Lakeside"))
        store.addCampsite(site("camp-2", "owner-2", "Ridge"))
        store.addReservation(
            Reservation(
                id = "res-1",
                campsiteId = "camp-1",
                campsiteName = "Lakeside",
                entrantId = "entrant-1",
                entrantName = "Ada Entrant",
                startDate = "2027-06-01",
                endDate = "2027-06-03",
                status = ReservationStatus.ACTIVE
            )
        )
    }

    @Test
    fun profileImageCanBeAddedUpdatedAndRemoved() {
        val controller = ProfileController(store)
        controller.setProfileImage("/tmp/ada.jpg")
        assertEquals("/tmp/ada.jpg", store.currentUser?.profileImageUri)

        controller.setProfileImage("/tmp/ada-new.jpg")
        assertEquals("/tmp/ada-new.jpg", store.currentUser?.profileImageUri)

        controller.setProfileImage(null)
        assertNull(store.currentUser?.profileImageUri)
    }

    @Test
    fun entrantPostsAReviewOnlyWithAReservation() {
        val reviews = ReviewController(store)
        assertNull(reviews.postReview("camp-1", 4, "Quiet and clean"))
        assertEquals(1, store.reviews.size)
        assertEquals(4.0, store.campsites.first { it.id == "camp-1" }.rating, 0.01)
        assertEquals(1, store.campsites.first { it.id == "camp-1" }.ratingCount)

        val rejected = reviews.postReview("camp-2", 5, "Never stayed here")
        assertEquals("You can review a campsite only after you have a reservation there", rejected)
    }

    @Test
    fun entrantCanMessageTheOwnerOfTheirReservation() {
        val messages = MessageController(store)
        assertNull(messages.send("res-1", "What time is check-in?"))
        assertEquals(1, store.messages.size)
        assertEquals("owner-1", store.messages.first().receiverId)

        val stranger = FakeCampStore("stranger")
        stranger.addUser(User(id = "stranger", entrant = true))
        store.reservations.forEach { stranger.addReservation(it) }
        store.campsites.forEach { stranger.addCampsite(it) }
        val denied = MessageController(stranger).send("res-1", "Hello")
        assertNotNull(denied)
    }

    @Test
    fun ownerCanListOtherOwnersCampsitesForAPeriod() {
        val form = LotteryFormState(
            name = "June sites",
            campsiteIds = listOf("camp-1", "camp-2"),
            registrationStart = "2027-05-01",
            registrationEnd = "2027-05-15",
            bookingStart = "2027-05-16",
            bookingEnd = "2027-06-30",
            winnerCount = "2"
        )
        val built = form.toLottery(
            id = "lot-shared",
            status = LotteryStatus.OPEN,
            entrants = emptyList(),
            validCampsiteIds = setOf("camp-1", "camp-2"),
            organizerCampsiteIds = setOf("camp-1")
        )
        assertNull(built.error)
        assertEquals(listOf("camp-1", "camp-2"), built.lottery?.campsiteIds)

        val onlyOthers = form.copy(campsiteIds = listOf("camp-2")).toLottery(
            id = "lot-bad",
            status = LotteryStatus.OPEN,
            entrants = emptyList(),
            validCampsiteIds = setOf("camp-1", "camp-2"),
            organizerCampsiteIds = setOf("camp-1")
        )
        assertNotNull(onlyOthers.error)
    }

    @Test
    fun adminRemovesRatingWithoutDeletingTheReview() {
        ReviewController(store).postReview("camp-1", 2, "Broken picnic table")
        val reviewId = store.reviews.first().id
        val admin = AdminController(store)

        assertNull(admin.removeRating(reviewId))
        assertTrue(store.reviews.first().ratingRemoved)
        assertEquals("Broken picnic table", store.reviews.first().body)
        assertEquals(0, store.campsites.first { it.id == "camp-1" }.ratingCount)
    }

    @Test
    fun adminRemovesAReviewThatBreaksPolicy() {
        ReviewController(store).postReview("camp-1", 1, "Not allowed")
        val reviewId = store.reviews.first().id

        assertNull(AdminController(store).removeReview(reviewId))
        assertTrue(store.reviews.isEmpty())
        assertEquals(0.0, store.campsites.first { it.id == "camp-1" }.rating, 0.01)
    }

    private fun site(id: String, ownerId: String, name: String) = Campsite(
        id = id,
        ownerId = ownerId,
        name = name,
        location = "Jasper",
        description = "Trees",
        maxOccupancy = 4,
        pricePerNight = 40.0
    )
}
