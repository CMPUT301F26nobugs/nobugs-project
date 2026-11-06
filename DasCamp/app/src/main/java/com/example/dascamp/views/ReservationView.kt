package com.example.dascamp.views

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.ModelView
import com.example.dascamp.model.Reservation
import androidx.compose.runtime.getValue

/**
 * Reservation View
 *
 * Watch the model and keep an up to date copy of reservation
 *
 * Collabrators:
 * DasCampRepository, ObservableModel, Reservation
 *
 * Used by screens: OwnerBookedDates, EntrantCampsiteScreen
 *
 * Stories: US 02.06.01, 01.01.05
 */
class ReservationView (private val repository: DasCampRepository ): ModelView {

    var reservations by mutableStateOf(listOf<Reservation>())

    init{
        repository.addView(this)
        update()
    }

    override fun update(){
        reservations = repository.reservations.toList()
    }

    fun remove(){
        repository.removeView(this)
    }
}