package com.example.dascamp.views

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.ModelView
import com.example.dascamp.model.OwnerMessage

/**
 * Watches the shared owner/entrant conversation currently open in the repository.
 *
 * Used by the entrant and owner message screens. US 01.14.01
 */
class MessageView(private val repository: DasCampRepository) : ModelView {
    var messages by mutableStateOf(listOf<OwnerMessage>())

    init {
        repository.addView(this)
        update()
    }

    override fun update() {
        val selectedReservation = repository.reservations
            .find { it.id == repository.openReservationId }
        val ownerId = repository.openMessageOwnerId.ifBlank {
            selectedReservation?.let { reservation ->
                repository.campsites.find { it.id == reservation.campsiteId }?.ownerId
            }.orEmpty()
        }
        val entrantId = repository.openMessageEntrantId.ifBlank {
            selectedReservation?.entrantId.orEmpty()
        }
        val legacyReservationIds = if (ownerId.isBlank() || entrantId.isBlank()) {
            emptySet()
        } else {
            repository.reservations
                .filter { reservation ->
                    reservation.entrantId == entrantId &&
                        repository.campsites.any {
                            it.id == reservation.campsiteId && it.ownerId == ownerId
                        }
                }
                .map { it.id }
                .toSet()
        }

        messages = repository.messages
            .filter { message ->
                (message.ownerId == ownerId && message.entrantId == entrantId) ||
                    (message.ownerId.isBlank() &&
                        message.entrantId.isBlank() &&
                        message.reservationId in legacyReservationIds)
            }
            .sortedBy { it.sentAt.toLongOrNull() ?: 0L }
    }

    fun remove() {
        repository.removeView(this)
    }
}
