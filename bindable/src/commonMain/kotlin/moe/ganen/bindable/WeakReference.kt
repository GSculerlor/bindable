package moe.ganen.bindable

public expect class WeakReference<T : Any>(referred: T) {
    public fun clear()

    public fun get(): T?
}
