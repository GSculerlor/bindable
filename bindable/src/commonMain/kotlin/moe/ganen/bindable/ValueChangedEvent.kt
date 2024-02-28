package moe.ganen.bindable

/**
 * An event fired when a value changes, providing the old and new value for reference.
 */
public data class ValueChangedEvent<T>(val oldValue: T, val newValue: T)
