package com.fireblocks.sdkdemo.ui.events

/**
 * Created by Fireblocks ltd. on 02/04/2023.
 */
interface EventListener {
    fun fireEvent(event: EventWrapper, count: Int)
    fun clearEventsCount()
}