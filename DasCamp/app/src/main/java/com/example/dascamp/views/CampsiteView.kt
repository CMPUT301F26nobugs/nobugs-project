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
 * Responsibilities:
 * - Watch the model and keep up-to-date copy of the data
 *
 * Collaborators: DasCampRepository(ObservableModel), the data class
 *
 * Used by screens:
 * Stories:
 *
 * @property repository the model to observe
 */
class CampsiteView(private val repository: DasCampRepository) : ModelView {

    /**
     * The latest snapshot of the campsites. This is Compose state, so composables that
     * read it recompose automatically when it changes. Read-only for screens in practice:
     * only [update] should assign to it.
     */
    var campsites by mutableStateOf(listOf<Campsite>())

    // Start watching as soon as this view is made
    init {
        repository.addView(this)
        update()
    }

    /**
     * Called by the repository whenever its data changes. Replaces [campsites] with a
     * copy of the repository's current list.
     */
    override fun update() {
        campsites = repository.campsites.toList()
    }

    /**
     * Stops watching the repository. After this, [campsites] is no longer refreshed.
     */
    fun remove() {
        repository.removeView(this)
    }
}