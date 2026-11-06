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
 * Entrants join or leave a waiting list during the registration period.
 * After registration ends, [drawWinners] selects entrants and creates
 * win/lose notifications.
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
     * Fails if registration is closed, the list is full, or the user
     * is already waiting.
     *
     * @param lottery the lottery to join
     * @param entrantName display name stored on the waitlist entry
     * @return null on success, or an error message if join fails
     */
    fun joinWaitlist(lottery: Lottery, entrantName: String): String? {
        val user = store.currentUser ?: return "Sign up first"
        if (lottery.status != LotteryStatus.OPEN) return "Lottery is closed"
        if (today() < lottery.registrationStart || today() > lottery.registrationEnd) {
            return "Registration is not open"
        }
        if (lottery.entrants.any { it.entrantId == user.id }) {
            return "Already on the waiting list"
        }
        val cap = lottery.waitlistCapacity
        if (cap != null && lottery.entrants.size >= cap) return "Waiting list is full"

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
        if (lottery.entrants.none { it.entrantId == user.id }) {
            return "Not on this waiting list"
        }
        store.updateLottery(
            lottery.copy(entrants = lottery.entrants.filterNot { it.entrantId == user.id })
        )
        return null
    }

    /**
     * Draws lottery winners after registration ends and notifies entrants.
     *
     * Selected entrants get [NotificationType.WIN]. Others who were still
     * waiting get [NotificationType.LOSE]. The lottery status becomes DRAWN.
     *
     * @param lottery the lottery to draw
     * @param random optional RNG so tests can be deterministic
     * @return null on success, or an error message if draw is not allowed
     */
    fun drawWinners(lottery: Lottery, random: Random = Random()): String? {
        if (lottery.status == LotteryStatus.DRAWN) return "Already drawn"
        if (today() <= lottery.registrationEnd) return "Registration has not ended"

        val waiting = lottery.entrants.filter { it.status == WaitlistStatus.WAITING }
        val winnerIds = waiting.shuffled(random).take(lottery.winnerCount).map { it.entrantId }.toSet()

        val updated = lottery.entrants.map { entry ->
            when {
                entry.entrantId in winnerIds -> entry.copy(status = WaitlistStatus.SELECTED)
                entry.status == WaitlistStatus.WAITING -> entry.copy(status = WaitlistStatus.NOT_SELECTED)
                else -> entry
            }
        }
        store.updateLottery(lottery.copy(status = LotteryStatus.DRAWN, entrants = updated))

        updated.forEach { entry ->
            val selected = when (entry.status) {
                WaitlistStatus.SELECTED -> true
                WaitlistStatus.NOT_SELECTED -> false
                else -> return@forEach
            }
            store.addNotification(
                NotificationsController.lotteryOutcome(
                    lotteryId = lottery.id,
                    lotteryName = lottery.name,
                    entrantId = entry.entrantId,
                    entrantName = entry.name,
                    selected = selected
                )
            )
        }
        return null
    }
}
