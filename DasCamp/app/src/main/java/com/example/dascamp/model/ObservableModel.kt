package com.example.dascamp.model

/**
 * A model that can be observed by one or more [ModelView]s (the subject in the
 * Observer pattern).
 *
 * Implementations keep a list of registered views and call [notifyViews] whenever
 * their state changes, so each view can refresh itself.
 */
interface ObservableModel {
    /**
     * Registers a view to be notified of future changes.
     *
     * @param view the view to add
     */
    fun addView(view: ModelView)

    /**
     * Unregisters a view so it no longer receives updates.
     *
     * @param view the view to remove
     */
    fun removeView(view: ModelView)

    /**
     * Calls [ModelView.update] on every registered view.
     *
     * Should be called by the model after its state changes.
     */
    fun notifyViews()
}

/**
 * Something that displays or reacts to an [ObservableModel] (the observer in the
 * Observer pattern).
 */
interface ModelView {
    /**
     * Called by the model when its state has changed.
     *
     * Implementations should re-read the model's data and refresh their display.
     */
    fun update()
}