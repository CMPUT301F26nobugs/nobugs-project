package com.example.dascamp.model

interface ObservableModel {
    fun addView(view: ModelView)
    fun removeView(view: ModelView)
    fun notifyViews()
}

interface ModelView {
    fun update()
}