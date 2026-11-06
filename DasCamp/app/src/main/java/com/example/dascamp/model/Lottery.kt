package com.example.dascamp.model

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
    val entrantId: String = "",
    val name: String = "",
    val joinedAt: String = "",
    val status: WaitlistStatus = WaitlistStatus.WAITING
)

/**
 * A lottery posted by the owner.
 * [campsiteIds] can hold more than one campsite
 * [winnerCount] is how many people are offered a spot
 */
data class Lottery(
    val id: String = "",
    val name: String = "",
    val campsiteIds: List<String> = emptyList(),
    val registrationStart: String = "",
    val registrationEnd: String = "",
    val bookingStart: String = "",
    val bookingEnd: String = "",
    val winnerCount: Int = 0,
    val waitlistCapacity: Int? = null,
    val status: LotteryStatus = LotteryStatus.OPEN,
    val entrants: List<WaitlistEntrant> = emptyList()
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
