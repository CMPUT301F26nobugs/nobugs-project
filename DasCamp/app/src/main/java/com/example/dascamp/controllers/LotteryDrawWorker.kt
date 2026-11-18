package com.example.dascamp.controllers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.WaitlistStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import com.google.android.gms.tasks.Tasks

/**
 * Checks lotteries in persistent background work and draws those whose
 * registration period has ended. Firestore transactions ensure that only one
 * device can draw a given lottery and create its notifications.
 */
class LotteryDrawWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    override fun doWork(): Result = try {
        val firestore = Firebase.firestore
        val openLotteries = Tasks.await(
            firestore.collection("lotteries")
                .whereEqualTo("status", LotteryStatus.OPEN.name)
                .get()
        )

        openLotteries.documents.forEach { document ->
            val lotteryRef = document.reference
            Tasks.await(firestore.runTransaction { transaction ->
                val current = transaction.get(lotteryRef).toObject(Lottery::class.java)
                    ?: return@runTransaction false
                if (current.status != LotteryStatus.OPEN || today() <= current.registrationEnd) {
                    return@runTransaction false
                }

                val waiting = current.entrants.filter { it.status == WaitlistStatus.WAITING }
                val winnerIds = waiting.shuffled(Random())
                    .take(current.winnerCount.coerceAtLeast(0))
                    .map { it.entrantId }
                    .toSet()
                val updatedEntrants = current.entrants.map { entrant ->
                    when {
                        entrant.entrantId in winnerIds -> entrant.copy(status = WaitlistStatus.SELECTED)
                        entrant.status == WaitlistStatus.WAITING -> entrant.copy(status = WaitlistStatus.NOT_SELECTED)
                        else -> entrant
                    }
                }

                transaction.set(
                    lotteryRef,
                    current.copy(status = LotteryStatus.DRAWN, entrants = updatedEntrants)
                )

                for (entrant in updatedEntrants) {
                    val selected = when (entrant.status) {
                        WaitlistStatus.SELECTED -> true
                        WaitlistStatus.NOT_SELECTED -> false
                        else -> continue
                    }
                    val notification = NotificationsController.lotteryOutcome(
                        lotteryId = current.id,
                        lotteryName = current.name,
                        entrantId = entrant.entrantId,
                        entrantName = entrant.name,
                        selected = selected
                    )
                    transaction.set(
                        firestore.collection("notifications").document(notification.id),
                        notification
                    )
                }
                true
            })
        }
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    companion object {
        const val IMMEDIATE_WORK_NAME = "lottery-draw-immediate"
        const val PERIODIC_WORK_NAME = "lottery-draw-periodic"
    }
}
