package moe.ganen.bindable

/**
 * Used by [Bindable.valueChanged] to provide the new value and old value when changes happened.
 */
data class ValueChangedEvent<out T>(val oldValue: T, val newValue: T)
