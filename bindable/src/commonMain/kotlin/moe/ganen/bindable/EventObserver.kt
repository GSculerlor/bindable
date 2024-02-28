package moe.ganen.bindable

/**
 * An observer that will be called when value changed.
 * This interface should not be implemented directly, but rather used as a receiver in [Bindable.bindDisabledChanged]
 * and [Bindable.bindDisabledChanged].
 */
public fun interface EventObserver<T> {
    public fun observe(value: T)
}
