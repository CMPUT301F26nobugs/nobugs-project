package com.example.dascamp.views

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.ModelView
import com.example.dascamp.model.Notification

/** Observes repository notification changes and exposes Compose state to screens. */
class NotificationView(private val repository: DasCampRepository) : ModelView {

    var notifications by mutableStateOf(emptyList<Notification>())
        private set

    init {
        repository.addView(this)
        update()
    }

    override fun update() {
        notifications = repository.notifications.toList()
    }

    fun remove() {
        repository.removeView(this)
    }
}
