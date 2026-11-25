package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.WaitlistEntrant
import com.example.dascamp.model.WaitlistStatus
import java.util.Random

/**
 * Handles lottery waitlists and winner draws for DasCamp.
 *
 * Entrants join or leave a waiting list through the booking period. After
 * registration ends, [drawWinners] selects entrants and fills declined or
 * expired offers from the waitlist.
 *
 * @property store the campsite / lottery data source
 */
class LotteryController(private val store: CampStore) {

    private fun today(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return formatter.format(java.util.Date())
    }

    /** True only while an OPEN lottery is accepting interest from entrants. */
    fun registrationOpen(lottery: Lottery): Boolean =
        lottery.status == LotteryStatus.OPEN &&
                today() >= lottery.registrationStart && today() <= lottery.registrationEnd

    /** Waitlist membership remains open through the booking period. */
    fun waitlistOpen(lottery: Lottery): Boolean =
        lottery.status != LotteryStatus.COMPLETED &&
                today() >= lottery.registrationStart && today() <= lottery.bookingEnd
    /**
     * Returns every entrant currently on the given lottery waitlist.
     *
     * @param lotteryId the lottery document id
     * @return the waitlist entries, or an empty list if the lottery is missing
     */
    fun waitlist(lotteryId: String): List<WaitlistEntrant> =
        store.lotteries.find { it.id == lotteryId }?.entrants.orEmpty()

    /**
     * Adds the signed-in user to a lottery waiting list.
     *
     * Fails if the booking period is closed, the list is full, or the user
     * is already waiting.
     *
     * @param lottery the lottery to join
     * @param entrantName display name stored on the waitlist entry
     * @return null on success, or an error message if join fails
     */
    fun joinWaitlist(lottery: Lottery, entrantName: String): String? {
        val user = store.currentUser ?: return "Sign up first"
        if (!waitlistOpen(lottery)) return "Lottery is closed"
        if (lottery.entrants.any { it.entrantId == user.id }) {
            return "Already on the waiting list"
        }
        val cap = lottery.waitlistCapacity
        val waitingCount = lottery.entrants.count { it.status == WaitlistStatus.WAITING }
        if (cap != null && waitingCount >= cap) return "Waiting list is full"

        store.updateLottery(
            lottery.copy(
                entrants = lottery.entrants + WaitlistEntrant(
                    entrantId = user.id,
                    name = entrantName.ifBlank { user.displayName() },
                    joinedAt = System.currentTimeMillis().toString(),
                    status = WaitlistStatus.WAITING
                )
            )
        )
        return null
    }

    /**
     * Removes the signed-in user from a lottery waiting list.
     *
     * @param lottery the lottery to leave
     * @return null on success, or an error message if leave fails
     */
    fun leaveWaitlist(lottery: Lottery): String? {
        val user = store.currentUser ?: return "Sign up first"
        if (!waitlistOpen(lottery)) return "Lottery is closed"
        if (lottery.entrants.none {
                it.entrantId == user.id && it.status == WaitlistStatus.WAITING
            }) {
            return "Not on this waiting list"
        }
        store.updateLottery(
            lottery.copy(entrants = lottery.entrants.filterNot { it.entrantId == user.id })
        )
        return null
    }

    /**
     * Draws initial winners after registration closes, draws replacements for
     * declined or expired offers, and notifies never-selected entrants when
     * the booking period ends.
     *
     * Newly selected entrants get [NotificationType.WIN]. Waiting entrants
     * remain eligible until booking ends, when they receive [NotificationType.LOSE].
     *
     * @param lottery the lottery to draw
     * @param random optional RNG so tests can be deterministic
     * @return null on success, or an error message if draw is not allowed
     */
    fun drawWinners(lottery: Lottery, random: Random = Random()): String? {
        if (lottery.status == LotteryStatus.COMPLETED) return "Lottery is complete"
        if (lottery.status == LotteryStatus.OPEN && today() <= lottery.registrationEnd) {
            return "Registration has not ended"
        }

        val now = System.currentTimeMillis()
        val bookingEnded = today() > lottery.bookingEnd
        val expired = lottery.entrants.map { entrant ->
            if (entrant.status != WaitlistStatus.SELECTED) entrant
            else {
                val deadline = entrant.responseDeadlineAt ?: responseDeadline(lottery, now)
                if (bookingEnded || deadline <= now) {
                    entrant.copy(status = WaitlistStatus.EXPIRED, responseDeadlineAt = null)
                } else entrant.copy(responseDeadlineAt = deadline)
            }
        }

        if (bookingEnded) {
            val finalized = expired.map { entrant ->
                if (entrant.status == WaitlistStatus.WAITING) {
                    store.addNotification(
                        NotificationsController.lotteryOutcome(
                            lottery.id, lottery.name, entrant.entrantId, entrant.name, selected = false
                        )
                    )
                    entrant.copy(status = WaitlistStatus.NOT_SELECTED)
                } else entrant
            }
            store.updateLottery(lottery.copy(status = LotteryStatus.COMPLETED, entrants = finalized))
            return null
        }

        val occupiedOffers = expired.count {
            it.status == WaitlistStatus.SELECTED || it.status == WaitlistStatus.BOOKED
        }
        val vacancies = (lottery.winnerCount - occupiedOffers).coerceAtLeast(0)
        val selectedIds = expired
            .filter { it.status == WaitlistStatus.WAITING }
            .shuffled(random)
            .take(vacancies)
            .map { it.entrantId }
            .toSet()
        val updated = expired.map { entrant ->
            if (entrant.entrantId in selectedIds) {
                entrant.copy(
                    status = WaitlistStatus.SELECTED,
                    responseDeadlineAt = responseDeadline(lottery, now)
                )
            } else entrant
        }
        store.updateLottery(lottery.copy(status = LotteryStatus.DRAWN, entrants = updated))
        updated.filter { it.entrantId in selectedIds }.forEach { entrant ->
            store.addNotification(
                NotificationsController.lotteryOutcome(
                    lotteryId = lottery.id,
                    lotteryName = lottery.name,
                    entrantId = entrant.entrantId,
                    entrantName = entrant.name,
                    selected = true
                )
            )
        }
        return null
    }

    private fun responseDeadline(lottery: Lottery, now: Long): Long {
        val windowEnd = now + lottery.responseWindowHours.coerceAtLeast(1).toLong() * 60L * 60L * 1000L
        val endOfBooking = try {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = parser.parse(lottery.bookingEnd) ?: return windowEnd
            java.util.Calendar.getInstance().apply {
                time = date
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }.timeInMillis
        } catch (_: Exception) {
            windowEnd
        }
        return minOf(windowEnd, endOfBooking)
    }

}
