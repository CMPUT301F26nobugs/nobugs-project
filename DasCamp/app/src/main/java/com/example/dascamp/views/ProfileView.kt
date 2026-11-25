package com.example.dascamp.views

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.ModelView
import com.example.dascamp.model.User
import com.google.firebase.firestore.ListenerRegistration

/**
 * PROFILE VIEW
 *
 * Responsibilities:
 * - Watch the model and keep up-to-date copy of the data
 *
 * Collaborators: DasCampRepository(ObservableModel), the data class
 *
 * Used by screens: EntrantProfileScreen
 * Stories: US 01.07.03, US 01.08.02
 */
class ProfileView(private val repository: DasCampRepository,
                  private val entrantId: String
) : ModelView {
    var profile by mutableStateOf<User?>(null)

    // Start watching as soon as this view is made
    init {
        repository.addView(this)
        update()
    }

    // Called by the model every time its data changes
    override fun update() {
        profile = repository.currentUser
    }

    // Stop watching
    fun remove() {
        repository.removeView(this)
    }
}