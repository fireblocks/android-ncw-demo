package com.fireblocks.sdkdemo.ui.events

/**
 * Created by Fireblocks Ltd. on 02/04/2023.
 */
interface EventListener {
    fun fireEvent(event: EventWrapper, count: Int)
    fun clearEventsCount() {}
}