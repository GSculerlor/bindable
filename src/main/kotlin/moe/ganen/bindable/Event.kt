package moe.ganen.bindable

/**
 * A Kotlin equivalent of C#'s event, kind of.
 */
internal class Event<T> {
    private val observers = mutableSetOf<Action<T>>()

    operator fun plusAssign(observer: Action<T>) {
        observers.add(observer)
    }

    operator fun minusAssign(observer: Action<T>) {
        observers.remove(observer)
    }

    operator fun invoke(value: T) {
        for (observer in observers)
            observer(value)
    }

    internal fun clear() {
        observers.clear()
    }
}

internal typealias Action<T> = (T) -> Unit
