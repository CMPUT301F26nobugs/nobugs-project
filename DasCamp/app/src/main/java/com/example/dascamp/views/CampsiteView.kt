package com.example.dascamp.views

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.ModelView

/**
 * CAMPSITE VIEW
 *
 * Responsibilites:
 * - Watch the model and keep up-to-date copy of the data
 *
 * Collaborators: DasCampRepository(ObservableModel), the data class
 *
 * Used by screens: add what screens use this
 * Stories: add your user story here
 */

class CampsiteView(private val repository: DasCampRepository) : ModelView {

    // What the screens read. Changing this makes the screens reload.
    var campsites by mutableStateOf(listOf<Campsite>())

    // Start watching as soon as this view is made
    init {
        repository.addView(this)
        update()
    }

    // Called by the model every time its data changes
    override fun update() {
        campsites = repository.campsites.toList()
    }

    // Stop watching
    fun remove() {
        repository.removeView(this)
    }
}