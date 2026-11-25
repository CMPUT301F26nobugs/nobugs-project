package com.example.dascamp.views

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.ModelView
import com.example.dascamp.model.OwnerMessage

/**
 * Watches messages for the reservation currently open in [DasCampRepository.openReservationId].
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
        val reservationId = repository.openReservationId
        messages = repository.messages
            .filter { it.reservationId == reservationId }
            .sortedBy { it.sentAt.toLongOrNull() ?: 0L }
    }

    fun remove() {
        repository.removeView(this)
    }
}
