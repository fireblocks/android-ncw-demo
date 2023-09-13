package com.fireblocks.sdkdemo.ui.events

import com.fireblocks.sdk.events.Event

/**
 * Created by Fireblocks Ltd. on 13/04/2023.
 */
class EventWrapper(val event: Event, val index: Int, val timestamp: Long) {

    override fun toString(): String {
        return "EventWrapper(event=$event, index=$index)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventWrapper

        if (event != other.event) return false
        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = event.hashCode()
        result = 31 * result + index
        return result
    }


}