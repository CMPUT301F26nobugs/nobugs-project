package com.example.dascamp.controllers

import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.LotteryOwnerApproval
import com.example.dascamp.model.LotteryProposal
import com.example.dascamp.model.LotteryProposalStatus
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.NotificationType
import com.example.dascamp.model.OwnerApprovalStatus
import com.google.android.gms.tasks.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class LotteryProposalSubmission(
    val task: Task<Void>? = null,
    val error: String? = null
)

/** Coordinates shared-lottery proposals, owner responses, and overlap checks. */
class LotteryProposalController(private val store: CampStore) {

    /** Defaults the approval date to the day before registration begins. */
    fun defaultApprovalDeadline(registrationStart: String): String? =
        shiftDate(registrationStart, -1)

    /**
     * Checks pending proposals and published open/drawn lotteries for a
     * shared campsite availability/booking date intersection.
     */
    fun validateNoOverlap(candidate: Lottery, editingLotteryId: String? = null): String? {
        val published = store.lotteries.filter {
            it.id != editingLotteryId &&
                (it.status == LotteryStatus.OPEN || it.status == LotteryStatus.DRAWN)
        }
        val pendingDrafts = store.lotteryProposals
            .filter { it.status == LotteryProposalStatus.PENDING }
            .map { it.lotteryDraft }
        val conflict = (published + pendingDrafts).firstOrNull {
            bookingWindowsConflict(candidate, it)
        }
        return conflict?.let {
            "A selected campsite already has a lottery with overlapping available booking dates."
        }
    }

    /** Builds and submits a pending proposal for a multi-owner lottery. */
    fun createProposal(
        lottery: Lottery,
        approvalDeadlineDate: String
    ): LotteryProposalSubmission {
        val creator = store.currentUser
            ?: return LotteryProposalSubmission(error = "Sign up first")
        if (!creator.owner) {
            return LotteryProposalSubmission(error = "Only campsite owners can create proposals")
        }
        val deadline = approvalDeadlineMillis(approvalDeadlineDate)
            ?: return LotteryProposalSubmission(error = "Choose a valid approval deadline")
        val latestAllowed = defaultApprovalDeadline(lottery.registrationStart)
            ?: return LotteryProposalSubmission(error = "Enter a valid registration start date")
        if (approvalDeadlineDate > latestAllowed ||
            approvalDeadlineDate >= lottery.registrationStart
        ) {
            return LotteryProposalSubmission(
                error = "The approval deadline must be before registration starts."
            )
        }
        validateNoOverlap(lottery)?.let {
            return LotteryProposalSubmission(error = it)
        }

        val selectedSites = lottery.campsiteIds.mapNotNull { campsiteId ->
            store.campsites.firstOrNull { it.id == campsiteId }
        }
        val sitesByOwner = selectedSites.groupBy { it.ownerId }
        if (sitesByOwner.size < 2 || sitesByOwner.keys.any { it.isBlank() }) {
            return LotteryProposalSubmission(
                error = "Select campsites from at least two owners."
            )
        }
        if (creator.id !in sitesByOwner.keys) {
            return LotteryProposalSubmission(error = "Include one of your own campsites.")
        }

        val proposalId = lottery.id
        val approvals = sitesByOwner.map { (ownerId, sites) ->
            val ownerName = store.userById(ownerId)?.displayName()
                ?.ifBlank { null } ?: "Campsite owner"
            LotteryOwnerApproval(
                ownerId = ownerId,
                ownerName = ownerName,
                campsiteIds = sites.map { it.id },
                status = if (ownerId == creator.id) OwnerApprovalStatus.APPROVED
                else OwnerApprovalStatus.PENDING
            )
        }
        val proposal = LotteryProposal(
            id = proposalId,
            creatorId = creator.id,
            creatorName = creator.displayName(),
            lotteryDraft = lottery.copy(id = proposalId, status = LotteryStatus.OPEN),
            ownerApprovals = approvals,
            approvalDeadlineAt = deadline,
            createdAt = System.currentTimeMillis()
        )
        val requestNotifications = proposal.notifications(
            type = NotificationType.LOTTERY_USE_REQUEST,
            message = "Approval requested for shared lottery \"${lottery.name}\".",
            senderId = creator.id,
            recipientIds = approvals
                .filter { it.ownerId != creator.id }
                .map { it.ownerId }
        )
        return LotteryProposalSubmission(
            task = store.createLotteryProposal(proposal, requestNotifications)
        )
    }

    fun respond(proposalId: String, ownerId: String, approve: Boolean) =
        store.respondToLotteryProposal(proposalId, ownerId, approve)

    fun cancel(proposalId: String, creatorId: String) =
        store.cancelLotteryProposal(proposalId, creatorId)

    private fun bookingWindowsConflict(first: Lottery, second: Lottery): Boolean {
        val sharedSites = first.campsiteIds.toSet() intersect second.campsiteIds.toSet()
        return sharedSites.any { campsiteId ->
            val campsite = store.campsites.firstOrNull { it.id == campsiteId }
                ?: return@any false
            campsite.availability.any { available ->
                available.start.isNotBlank() && available.end.isNotBlank() &&
                    maxOf(available.start, first.bookingStart, second.bookingStart) <=
                    minOf(available.end, first.bookingEnd, second.bookingEnd)
            }
        }
    }

    private fun approvalDeadlineMillis(date: String): Long? {
        val parsed = parseDate(date) ?: return null
        return Calendar.getInstance().apply {
            time = parsed
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun shiftDate(value: String, days: Int): String? {
        val parsed = parseDate(value) ?: return null
        val calendar = Calendar.getInstance().apply {
            time = parsed
            add(Calendar.DAY_OF_MONTH, days)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    private fun parseDate(value: String): Date? = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }.parse(value)
    }.getOrNull()
}
