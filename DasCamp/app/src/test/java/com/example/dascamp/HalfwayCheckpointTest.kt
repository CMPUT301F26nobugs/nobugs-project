package com.example.dascamp

import com.example.dascamp.controllers.CampsiteController
import com.example.dascamp.controllers.LotteryController
import com.example.dascamp.controllers.ReservationController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.CampsiteFormState
import com.example.dascamp.model.DateRange
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.LotteryFormState
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.NotificationType
import com.example.dascamp.model.Policy
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.User
import com.example.dascamp.model.WaitlistEntrant
import com.example.dascamp.model.WaitlistStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Random

/**
 * JUnit tests for open Half-way Checkpoint user stories.
 *
 * Follows Lab 6 style: [Before] builds a fresh fake store and controllers,
 * each [Test] checks one behaviour with assertEquals / assertTrue.
 */
class HalfwayCheckpointTest {

    private lateinit var store: FakeCampStore
    private lateinit var campsiteController: CampsiteController
    private lateinit var scheduleController: ScheduleController
    private lateinit var lotteryController: LotteryController
    private lateinit var reservationController: ReservationController

    private val ownerId = "owner-1"
    private val entrantId = "entrant-1"

    private fun sampleCampsite(): Campsite = Campsite(
        id = "camp-1",
        ownerId = ownerId,
        name = "Lake View",
        location = "Banff",
        description = "Fire pit, lake access",
        maxOccupancy = 4,
        pricePerNight = 80.0,
        rules = "Quiet after 10pm",
        cancellationPolicy = "48h notice",
        tags = listOf("lake", "pets"),
        latitude = 51.18,
        longitude = -115.57,
        availability = listOf(DateRange("2027-06-01", "2027-08-31"))
    )

    private fun openLottery(): Lottery = Lottery(
        id = "lot-1",
        name = "Summer draw",
        campsiteIds = listOf("camp-1"),
        registrationStart = "2020-01-01",
        registrationEnd = "2099-01-01",
        bookingStart = "2099-01-02",
        bookingEnd = "2099-12-31",
        winnerCount = 1,
        status = LotteryStatus.OPEN
    )

    // Set up a fresh store before every single test
    @Before
    fun setUp() {
        store = FakeCampStore(uid = entrantId)
        store.addUser(User(id = entrantId, firstName = "Ada", lastName = "Entrant", entrant = true))
        store.addUser(User(id = ownerId, firstName = "Owen", lastName = "Owner", owner = true))
        store.addCampsite(sampleCampsite())

        campsiteController = CampsiteController(store)
        scheduleController = ScheduleController(store)
        lotteryController = LotteryController(store)
        reservationController = ReservationController(store)
    }

    // -------- Owner: US 02.01.* --------

    @Test
    fun toCampsite_validForm_registerSuccessful() {
        // US 02.01.01 — register campsite
        val form = CampsiteFormState(
            name = "Pine Ridge",
            location = "Jasper",
            description = "Trees",
            maxOccupancy = "6",
            pricePerNight = "100",
            rules = "No drones",
            cancellationPolicy = "Flexible",
            tags = "forest",
            latitude = "52.87",
            longitude = "-118.08",
            availability = listOf(DateRange("2027-07-01", "2027-07-31"))
        )

        val built = form.toCampsite("camp-new", ownerId, 0.0, 0)

        assertNull(built.error)
        assertEquals("Pine Ridge", built.campsite!!.name)
    }

    @Test
    fun toCampsite_missingName_unsuccessful() {
        // US 02.01.02 — required details must be present
        val form = CampsiteFormState(
            name = "",
            location = "Banff",
            description = "Amenities",
            maxOccupancy = "4",
            pricePerNight = "50",
            rules = "Rules",
            cancellationPolicy = "Policy",
            latitude = "51.0",
            longitude = "-115.0",
            availability = listOf(DateRange("2027-06-01", "2027-06-30"))
        )

        val built = form.toCampsite("camp-x", ownerId, 0.0, 0)

        assertNull(built.campsite)
        assertEquals("Enter a campsite name", built.error)
    }

    @Test
    fun updateCampsite_newName_successful() {
        // US 02.01.03 — update campsite details
        store.updateCampsite(sampleCampsite().copy(name = "Lake View Updated"))

        assertEquals("Lake View Updated", store.campsites.first().name)
    }

    @Test
    fun viewCampsite_existingId_returnsDetails() {
        // US 02.01.04 — view campsite details
        val campsite = store.campsites.find { it.id == "camp-1" }

        assertNotNull(campsite)
        assertEquals("Banff", campsite!!.location)
        assertEquals(4, campsite.maxOccupancy)
    }

    // -------- Owner: US 02.04.* --------

    @Test
    fun toLottery_validForm_listSuccessful() {
        // US 02.04.01 — list campsite in a lottery
        val built = LotteryFormState(
            name = "July lottery",
            campsiteIds = listOf("camp-1"),
            registrationStart = "2027-05-01",
            registrationEnd = "2027-05-15",
            bookingStart = "2027-05-16",
            bookingEnd = "2027-06-30",
            winnerCount = "2"
        ).toLottery("lot-x", LotteryStatus.OPEN, emptyList(), setOf("camp-1"))

        assertNull(built.error)
        assertEquals(listOf("camp-1"), built.lottery!!.campsiteIds)
    }

    @Test
    fun toLottery_registrationEndsBeforeStart_unsuccessful() {
        // US 02.04.03 — registration period
        val built = LotteryFormState(
            name = "Bad reg",
            campsiteIds = listOf("camp-1"),
            registrationStart = "2027-05-15",
            registrationEnd = "2027-05-01",
            bookingStart = "2027-05-16",
            bookingEnd = "2027-06-01",
            winnerCount = "1"
        ).toLottery("l", LotteryStatus.OPEN, emptyList(), setOf("camp-1"))

        assertNull(built.lottery)
        assertEquals("Registration has to end on or after it starts", built.error)
    }

    @Test
    fun toLottery_bookingStartsDuringRegistration_unsuccessful() {
        // US 02.04.04 — booking period
        val built = LotteryFormState(
            name = "Bad book",
            campsiteIds = listOf("camp-1"),
            registrationStart = "2027-05-01",
            registrationEnd = "2027-05-15",
            bookingStart = "2027-05-10",
            bookingEnd = "2027-06-01",
            winnerCount = "1"
        ).toLottery("l", LotteryStatus.OPEN, emptyList(), setOf("camp-1"))

        assertEquals("Booking has to start after registration ends", built.error)
    }

    @Test
    fun toLottery_zeroWinners_unsuccessful() {
        // US 02.04.05 — number of winners
        val built = LotteryFormState(
            name = "No winners",
            campsiteIds = listOf("camp-1"),
            registrationStart = "2027-05-01",
            registrationEnd = "2027-05-15",
            bookingStart = "2027-05-16",
            bookingEnd = "2027-06-01",
            winnerCount = "0"
        ).toLottery("l", LotteryStatus.OPEN, emptyList(), setOf("camp-1"))

        assertEquals("Enter how many winners to draw", built.error)
    }

    @Test
    fun updateLottery_winnerCount_successful() {
        // US 02.04.06 — update lottery listing
        store.addLottery(openLottery())
        store.updateLottery(store.lotteries.first().copy(winnerCount = 3))

        assertEquals(3, store.lotteries.first().winnerCount)
    }

    // -------- Owner: US 02.05 / 02.06 / 02.07 / 02.09 / 02.11 --------

    @Test
    fun drawWinners_afterRegistration_successful() {
        // US 02.05.01 — automatic / post-registration draw
        val lottery = openLottery().copy(
            registrationEnd = "2020-01-01",
            bookingStart = "2020-01-02",
            bookingEnd = "2020-12-31",
            entrants = listOf(
                WaitlistEntrant("e1", "A", status = WaitlistStatus.WAITING),
                WaitlistEntrant("e2", "B", status = WaitlistStatus.WAITING)
            )
        )
        store.addLottery(lottery)

        val error = lotteryController.drawWinners(lottery, Random(1))

        assertNull(error)
        assertEquals(LotteryStatus.DRAWN, store.lotteries.first().status)
        assertEquals(1, store.lotteries.first().entrants.count { it.status == WaitlistStatus.SELECTED })
    }

    @Test
    fun bookedReservations_activeStay_returned() {
        // US 02.06.01 — view booked dates
        store.addReservation(
            Reservation(
                id = "r1",
                campsiteId = "camp-1",
                campsiteName = "Lake View",
                entrantId = entrantId,
                entrantName = "Ada",
                startDate = "2027-06-10",
                endDate = "2027-06-12",
                status = ReservationStatus.ACTIVE
            )
        )

        val booked = scheduleController.bookedReservations("camp-1", store.reservations)

        assertEquals(1, booked.size)
        assertEquals(true, scheduleController.isDateBooked("camp-1", "2027-06-11"))
    }

    @Test
    fun ownerActive_futureReservation_successful() {
        // US 02.07.01
        val ownerStore = FakeCampStore(uid = ownerId)
        ownerStore.addCampsite(sampleCampsite())
        ownerStore.addReservation(
            Reservation(
                "r1", "camp-1", "Lake View", entrantId, "Ada",
                "2099-06-01", "2099-06-03", ReservationStatus.ACTIVE
            )
        )

        assertEquals(1, ReservationController(ownerStore).ownerActive().size)
    }

    @Test
    fun ownerPast_endedReservation_successful() {
        // US 02.07.02
        val ownerStore = FakeCampStore(uid = ownerId)
        ownerStore.addCampsite(sampleCampsite())
        ownerStore.addReservation(
            Reservation(
                "r1", "camp-1", "Lake View", entrantId, "Ada",
                "2020-06-01", "2020-06-03", ReservationStatus.ACTIVE
            )
        )

        assertEquals(1, ReservationController(ownerStore).ownerPast().size)
    }

    @Test
    fun ownerCancelled_cancelledReservation_successful() {
        // US 02.07.03
        val ownerStore = FakeCampStore(uid = ownerId)
        ownerStore.addCampsite(sampleCampsite())
        ownerStore.addReservation(
            Reservation(
                "r1", "camp-1", "Lake View", entrantId, "Ada",
                "2027-06-01", "2027-06-03", ReservationStatus.CANCELLED
            )
        )

        assertEquals(1, ReservationController(ownerStore).ownerCancelled().size)
    }

    @Test
    fun waitlist_joinedEntrant_visible() {
        // US 02.09.01 — view waitlist
        store.addLottery(
            openLottery().copy(
                entrants = listOf(
                    WaitlistEntrant(entrantId, "Ada", status = WaitlistStatus.WAITING)
                )
            )
        )

        assertEquals(1, lotteryController.waitlist("lot-1").size)
        assertEquals("Ada", lotteryController.waitlist("lot-1").first().name)
    }

    @Test
    fun reserveDates_rulesNotAccepted_unsuccessful() {
        // US 02.11.01 — must confirm rules before reserving
        val error = scheduleController.reserveDates(
            campsite = sampleCampsite(),
            lottery = null,
            entrantId = entrantId,
            entrantName = "Ada",
            startDate = "2027-06-01",
            endDate = "2027-06-03",
            guestCount = 2,
            rulesAccepted = false
        )

        assertEquals("Confirm You have read the rules and policies", error)
    }

    @Test
    fun reserveDates_rulesAccepted_successful() {
        // US 02.11.01 — success path
        val error = scheduleController.reserveDates(
            campsite = sampleCampsite(),
            lottery = null,
            entrantId = entrantId,
            entrantName = "Ada",
            startDate = "2027-06-01",
            endDate = "2027-06-03",
            guestCount = 2,
            rulesAccepted = true
        )

        assertNull(error)
        assertEquals(1, store.reservations.size)
    }

    // -------- Entrant: US 01.* --------

    @Test
    fun searchCampsites_byLocation_successful() {
        // supports US 01.01.05 details discovery via search pipeline
        val results = campsiteController.searchCampsites(location = "Banff")

        assertEquals(1, results.size)
        assertEquals("Lake View", results.first().name)
    }

    @Test
    fun joinWaitlist_openLottery_successful() {
        // US 01.01.06 / US 01.02.01
        store.addLottery(openLottery())

        val error = lotteryController.joinWaitlist(store.lotteries.first(), "Ada Entrant")

        assertNull(error)
        assertEquals(1, store.lotteries.first().entrants.size)
    }

    @Test
    fun leaveWaitlist_joinedEntrant_successful() {
        // US 01.02.02
        store.addLottery(openLottery())
        lotteryController.joinWaitlist(store.lotteries.first(), "Ada")

        val error = lotteryController.leaveWaitlist(store.lotteries.first())

        assertNull(error)
        assertEquals(0, store.lotteries.first().entrants.size)
    }

    @Test
    fun drawWinners_selectedEntrant_getsWinNotification() {
        // US 01.03.01
        val lottery = openLottery().copy(
            registrationEnd = "2020-01-01",
            bookingStart = "2020-01-02",
            bookingEnd = "2020-12-31",
            entrants = listOf(
                WaitlistEntrant(entrantId, "Ada", status = WaitlistStatus.WAITING)
            )
        )
        store.addLottery(lottery)
        lotteryController.drawWinners(lottery, Random(0))

        assertTrue(store.notifications.any { it.type == NotificationType.WIN })
    }

    @Test
    fun drawWinners_notSelectedEntrant_getsLoseNotification() {
        // US 01.03.02
        val lottery = openLottery().copy(
            registrationEnd = "2020-01-01",
            bookingStart = "2020-01-02",
            bookingEnd = "2020-12-31",
            winnerCount = 1,
            entrants = listOf(
                WaitlistEntrant("winner", "W", status = WaitlistStatus.WAITING),
                WaitlistEntrant(entrantId, "Ada", status = WaitlistStatus.WAITING)
            )
        )
        store.addLottery(lottery)
        lotteryController.drawWinners(lottery, Random(42))

        assertTrue(store.notifications.any { it.type == NotificationType.LOSE })
    }

    @Test
    fun isDateAvailable_freeDate_successful() {
        // US 01.04.01
        assertTrue(scheduleController.isDateAvailable(sampleCampsite(), "2027-06-15"))
    }

    @Test
    fun isDateAvailable_bookedDate_unsuccessful() {
        // US 01.04.01
        store.addReservation(
            Reservation(
                "r1", "camp-1", "Lake View", "other", "Other",
                "2027-06-15", "2027-06-16", ReservationStatus.ACTIVE
            )
        )

        assertFalse(scheduleController.isDateAvailable(sampleCampsite(), "2027-06-15"))
    }

    @Test
    fun reserve_selectedEntrant_sendsConfirmation() {
        // US 01.05.01
        store.addLottery(
            openLottery().copy(
                bookingStart = "2020-01-01",
                bookingEnd = "2099-12-31",
                entrants = listOf(
                    WaitlistEntrant(entrantId, "Ada", status = WaitlistStatus.SELECTED)
                )
            )
        )

        val error = reservationController.reserve(
            store.lotteries.first(),
            sampleCampsite(),
            "2027-06-01",
            "2027-06-03"
        )

        assertNull(error)
        assertTrue(store.notifications.any { it.message.contains("Reservation confirmed") })
    }

    @Test
    fun updateUser_emailAndPhone_successful() {
        // US 01.07.03
        val user = store.currentUser!!
        store.updateUser(user.copy(email = "ada@ualberta.ca", phoneNumber = "780-555-0100"))

        assertEquals("ada@ualberta.ca", store.currentUser!!.email)
        assertEquals("780-555-0100", store.currentUser!!.phoneNumber)
    }

    @Test
    fun policy_text_isVisible() {
        // US 01.11.01
        assertTrue(Policy.TEXT.isNotBlank())
        assertFalse(Policy.TEXT.contains("TODO", ignoreCase = true))
    }
}