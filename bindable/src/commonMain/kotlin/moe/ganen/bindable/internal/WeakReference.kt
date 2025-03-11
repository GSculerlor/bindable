package moe.ganen.bindable.internal

internal expect class WeakReference<T : Any>(referred: T) {
    fun clear()

    fun get(): T?
}
