package com.example.dascamp.model

/**
 * Dates are stored as yyyy-MM-dd
 */
data class DateRange(
    val start: String,
    val end: String
)

/**
 * One campsite listing owned by one organization
 * Entrant search reads this same type. Admin removal deletes one of these rows
 */
data class Campsite(
    val id: String,
    val ownerId: String,
    val name: String,
    val location: String,
    val description: String,
    val maxOccupancy: Int,
    val pricePerNight: Double,
    val rating: Double,
    val ratingCount: Int,
    val imageUri: String?,
    val rules: String,
    val cancellationPolicy: String,
    val tags: List<String>,
    val latitude: Double,
    val longitude: Double,
    val availability: List<DateRange>
)

/**
 * The add and edit screens share this draft so both forms ask for the same fields
 */
data class CampsiteFormState(
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val maxOccupancy: String = "",
    val pricePerNight: String = "",
    val rules: String = "",
    val cancellationPolicy: String = "",
    val tags: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val imageUri: String? = null,
    val availability: List<DateRange> = listOf(DateRange("", ""))
) {
    companion object {
        fun from(campsite: Campsite) = CampsiteFormState(
            name = campsite.name,
            location = campsite.location,
            description = campsite.description,
            maxOccupancy = campsite.maxOccupancy.toString(),
            pricePerNight = formatPrice(campsite.pricePerNight),
            rules = campsite.rules,
            cancellationPolicy = campsite.cancellationPolicy,
            tags = campsite.tags.joinToString(", "),
            latitude = campsite.latitude.toString(),
            longitude = campsite.longitude.toString(),
            imageUri = campsite.imageUri,
            availability = campsite.availability.ifEmpty { listOf(DateRange("", "")) }
        )
    }

    /**
     * Turns the form into a campsite
     * Rating is passed in so an edit cannot wipe reviews entrants already gave
     * Rules are required because the entrant has to accept them before reserving
     */
    fun toCampsite(
        id: String,
        ownerId: String,
        rating: Double,
        ratingCount: Int
    ): CampsiteBuild {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return CampsiteBuild(null, "Enter a campsite name")
        val trimmedLocation = location.trim()
        if (trimmedLocation.isEmpty()) {
            return CampsiteBuild(null, "Enter the location")
        }
        val trimmedDescription = description.trim()
        if (trimmedDescription.isEmpty()) {
            return CampsiteBuild(null, "Describe the amenities")
        }
        val occupancy = maxOccupancy.trim().toIntOrNull()
        if (occupancy == null || occupancy < 1) {
            return CampsiteBuild(null, "Enter a maximum occupancy of at least 1.")
        }
        val price = pricePerNight.trim().toDoubleOrNull()
        if (price == null || price < 0) {
            return CampsiteBuild(null, "Enter a price per night")
        }
        val trimmedRules = rules.trim()
        if (trimmedRules.isEmpty()) {
            return CampsiteBuild(null, "Enter the rules entrants must accept before they reserve")
        }
        val trimmedPolicy = cancellationPolicy.trim()
        if (trimmedPolicy.isEmpty()) {
            return CampsiteBuild(null, "Enter a cancellation policy")
        }
        val lat = latitude.trim().toDoubleOrNull()
        if (lat == null || lat !in -90.0..90.0) {
            return CampsiteBuild(null, "Enter a latitude between -90 and 90")
        }
        val lng = longitude.trim().toDoubleOrNull()
        if (lng == null || lng !in -180.0..180.0) {
            return CampsiteBuild(null, "Enter a longitude between -180 and 180")
        }
        val ranges = availability.filter { it.start.isNotBlank() || it.end.isNotBlank() }
        if (ranges.isEmpty()) {
            return CampsiteBuild(null, "Add availability range, such as 2027-05-01 to 2027-05-02.")
        }
        ranges.forEach { range ->
            if (invalidDate(range.start) || invalidDate(range.end)) {
                return CampsiteBuild(null, "Use this format 2027-05-01 for every availability range")
            }
            if (range.end < range.start) {
                return CampsiteBuild(null, "Each availability end date has to be on or after its start date")
            }
        }
        val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return CampsiteBuild(
            Campsite(
                id = id,
                ownerId = ownerId,
                name = trimmedName,
                location = trimmedLocation,
                description = trimmedDescription,
                maxOccupancy = occupancy,
                pricePerNight = price,
                rating = rating,
                ratingCount = ratingCount,
                imageUri = imageUri,
                rules = trimmedRules,
                cancellationPolicy = trimmedPolicy,
                tags = tagList,
                latitude = lat,
                longitude = lng,
                availability = ranges
            ),
            null
        )
    }
}

data class CampsiteBuild(val campsite: Campsite?, val error: String?)

enum class LotteryStatus { OPEN, DRAWN }

fun LotteryStatus.label(): String = when (this) {
    LotteryStatus.OPEN -> "Open"
    LotteryStatus.DRAWN -> "Drawn"
}

/**
 * Where one entrant sits on one lottery
 * [entrantId] is for the device id. [joinedAt] is kept so the list can be shown in join order
 */
enum class WaitlistStatus { WAITING, SELECTED, NOT_SELECTED, DECLINED, BOOKED }

fun WaitlistStatus.label(): String = when (this) {
    WaitlistStatus.WAITING -> "On the waiting list"
    WaitlistStatus.SELECTED -> "Selected"
    WaitlistStatus.NOT_SELECTED -> "Not selected"
    WaitlistStatus.DECLINED -> "Declined"
    WaitlistStatus.BOOKED -> "Reserved"
}

data class WaitlistEntrant(
    val entrantId: String,
    val name: String,
    val joinedAt: String,
    val status: WaitlistStatus
)

/**
 * A lottery posted by the owner.
 * [campsiteIds] can hold more than one campsite
 * [winnerCount] is how many people are offered a spot
 */
data class Lottery(
    val id: String,
    val name: String,
    val campsiteIds: List<String>,
    val registrationStart: String,
    val registrationEnd: String,
    val bookingStart: String,
    val bookingEnd: String,
    val winnerCount: Int,
    val waitlistCapacity: Int?,
    val status: LotteryStatus,
    val entrants: List<WaitlistEntrant>
)

data class LotteryFormState(
    val name: String = "",
    val campsiteIds: List<String> = emptyList(),
    val registrationStart: String = "",
    val registrationEnd: String = "",
    val bookingStart: String = "",
    val bookingEnd: String = "",
    val winnerCount: String = "5",
    val waitlistCapacity: String = ""
) {
    companion object {
        fun from(lottery: Lottery) = LotteryFormState(
            name = lottery.name,
            campsiteIds = lottery.campsiteIds,
            registrationStart = lottery.registrationStart,
            registrationEnd = lottery.registrationEnd,
            bookingStart = lottery.bookingStart,
            bookingEnd = lottery.bookingEnd,
            winnerCount = lottery.winnerCount.toString(),
            waitlistCapacity = lottery.waitlistCapacity?.toString().orEmpty()
        )
    }

    /**
     * Checks the registration period, the booking period, the winner count,
     * and the optional waitlist cap before a lottery is stored.
     * Booking has to start after registration ends: people are drawn first,
     * then the selected entrants reserve
     */
    fun toLottery(
        id: String,
        status: LotteryStatus,
        entrants: List<WaitlistEntrant>,
        validCampsiteIds: Set<String>
    ): LotteryBuild {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return LotteryBuild(null, "Enter a name for this lottery")
        if (campsiteIds.isEmpty()) {
            return LotteryBuild(null, "Choose at least one campsite")
        }
        if (campsiteIds.any { it !in validCampsiteIds }) {
            return LotteryBuild(null, "One of the selected campsites no longer exists")
        }
        if (invalidDate(registrationStart) || invalidDate(registrationEnd)) {
            return LotteryBuild(null, "Enter the registration period")
        }
        if (registrationEnd < registrationStart) {
            return LotteryBuild(null, "Registration has to end on or after it starts")
        }
        if (invalidDate(bookingStart) || invalidDate(bookingEnd)) {
            return LotteryBuild(null, "Enter the booking period")
        }
        if (bookingEnd < bookingStart) {
            return LotteryBuild(null, "Booking has to end on or after it starts")
        }
        if (bookingStart <= registrationEnd) {
            return LotteryBuild(null, "Booking has to start after registration ends")
        }
        val winners = winnerCount.trim().toIntOrNull()
        if (winners == null || winners < 1) {
            return LotteryBuild(null, "Enter how many winners to draw")
        }
        val capText = waitlistCapacity.trim()
        val capacity = if (capText.isEmpty()) null else capText.toIntOrNull()
        if (capText.isNotEmpty() && (capacity == null || capacity < 1)) {
            return LotteryBuild(null, "Waitlist capacity has to be at least 1, or blank for no limit")
        }
        if (capacity != null && capacity < entrants.size) {
            return LotteryBuild(
                null,
                "Capacity cannot be lower than ${entrants.size}"
            )
        }
        return LotteryBuild(
            Lottery(
                id = id,
                name = trimmedName,
                campsiteIds = campsiteIds.distinct(),
                registrationStart = registrationStart.trim(),
                registrationEnd = registrationEnd.trim(),
                bookingStart = bookingStart.trim(),
                bookingEnd = bookingEnd.trim(),
                winnerCount = winners,
                waitlistCapacity = capacity,
                status = status,
                entrants = entrants
            ),
            null
        )
    }
}

data class LotteryBuild(val lottery: Lottery?, val error: String?)

enum class ReservationStatus { ACTIVE, PAST, CANCELLED }

fun ReservationStatus.label(): String = when (this) {
    ReservationStatus.ACTIVE -> "Active"
    ReservationStatus.PAST -> "Past"
    ReservationStatus.CANCELLED -> "Cancelled"
}

/**
 * Reservation after the entrant is confirmed a spot, The owner reviews them and can reject an active one
 * [cancelReason] is what the owner sends with a rejection.
 */
data class Reservation(
    val id: String,
    val campsiteId: String,
    val campsiteName: String,
    val entrantId: String,
    val entrantName: String,
    val startDate: String,
    val endDate: String,
    val status: ReservationStatus,
    val lotteryId: String?,
    val cancelReason: String?
)

data class OwnerNotification(
    val id: String,
    val lotteryId: String?,
    val recipientName: String,
    val message: String,
    val sentAt: String
)

/**
 * A message from the owner to someone who reserved their campsite
 * Admin can review the same rows
 */
data class OwnerMessage(
    val id: String,
    val reservationId: String,
    val entrantName: String,
    val body: String,
    val sentAt: String
)

/**
 * The owner account shown on the dashboard and edited on the profile screen
 * An admin may also act as an owner with one profile
 */
data class OwnerProfile(
    val id: String,
    val organizationName: String,
    val contactName: String,
    val email: String,
    val phone: String
)

fun formatPrice(price: Double): String =
    if (price % 1.0 == 0.0) price.toInt().toString() else "%.2f".format(price)

private fun invalidDate(value: String): Boolean {
    if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)) return true
    val parts = value.split("-")
    val month = parts[1].toInt()
    val day = parts[2].toInt()
    return month !in 1..12 || day !in 1..31
}