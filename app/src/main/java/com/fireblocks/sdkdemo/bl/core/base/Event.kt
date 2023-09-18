package com.fireblocks.sdkdemo.bl.core.base

/**
 * Created by Fireblocks ltd. on 2020-01-29
 */
open class Event<out T>(private val content: T) {

    var isConsumed = false
        private set // Allow external read but not write

    private var onNotConsumed: ((T) -> Unit)? = null
    private var onConsumed: ((T) -> Unit)? = null

    /**
     * Returns the content and prevents its use again.
     */
    fun getIfNotConsumed(): T? {
        return if (isConsumed) {
            null
        } else {
            isConsumed = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content

    fun onNotConsumedYet(action: (T) -> Unit): Event<T> {
        onNotConsumed = action
        return this
    }

    fun onAlreadyConsumed(action: (T) -> Unit): Event<T> {
        onConsumed = action
        return this
    }

    fun consume() {
        val consumed = getIfNotConsumed()
        consumed?.let {
            onNotConsumed?.invoke(it)
        } ?: run {
            onConsumed?.invoke(peekContent())
        }

        onNotConsumed = null
        onConsumed = null
    }
}