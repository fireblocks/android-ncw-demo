package com.fireblocks.sdkdemo.ui.observers

/**
 * Created by Fireblocks ltd. on 30/03/2023.
 */
class ObservedData<T>(content: T?) {
    private val content: T
    private var hasBeenHandled = false

    init {
        requireNotNull(content) { "null values in Event are not allowed." }
        this.content = content
    }

    val contentIfNotHandled: T?
        get() = if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }

    fun peekContent(): T {
        return content
    }

    fun hasBeenHandled(): Boolean {
        return hasBeenHandled
    }
}