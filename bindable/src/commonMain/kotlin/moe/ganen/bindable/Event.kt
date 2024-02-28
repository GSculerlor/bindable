package moe.ganen.bindable

/**
 * A Kotlin equivalent of C#'s event, kind of.
 */
public class Event<T> {
    private val observers = mutableSetOf<EventObserver<T>>()

    public operator fun plusAssign(observer: EventObserver<T>) {
        observers.add(observer)
    }

    public operator fun minusAssign(observer: EventObserver<T>) {
        observers.remove(observer)
    }

    public operator fun invoke(value: T) {
        for (observer in observers)
            observer.observe(value)
    }

    internal fun clear() {
        observers.clear()
    }
}
