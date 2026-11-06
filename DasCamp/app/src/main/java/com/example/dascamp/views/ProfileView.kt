package com.example.dascamp.views

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.EntrantProfile
import com.example.dascamp.model.ModelView
import com.google.firebase.firestore.ListenerRegistration

/**
 * PROFILE VIEW
 *
 * Responsibilites:
 * - Watch the model and keep up-to-date copy of the data
 *
 * Collaborators: DasCampRepository(ObservableModel), the data class
 *
 * Used by screens: EntrantProfileScreens add what screens use this
 * Stories: US 01.07.03
 * add your user story here
 */
class ProfileView(private val repository: DasCampRepository,
                  private val entrantId: String
) : ModelView {
    var profile by mutableStateOf<EntrantProfile?>(null)

    // Start watching as soon as this view is made
    init {
        repository.addView(this)
        update()
    }

    // Called by the model every time its data changes
    override fun update() {
        profile = repository.entrants.find { it.id == entrantId }
    }

    // Stop watching
    fun remove() {
        repository.removeView(this)
    }
}