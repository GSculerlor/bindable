package moe.ganen.bindable

/**
 * [Bindable] extension function to bind multiple bindables to it.
 * @see [Bindable.bindTo]
 * @param them the foreign bindables.
 */
public fun <T> Bindable<T>.bindTo(vararg them: Bindable<T>) {
    them.forEach { it.bindTo(this) }
}

/**
 * Create new [Bindable] with initial value. This bindable will not be disabled by default.
 * @param value initial value of the bindable.
 */
public fun <T> Bindable(value: T): Bindable<T> = BindableImpl(value)

/**
 * Create new [Bindable] with initial value and disabled state.
 * @param value initial value of the bindable.
 * @param disabledByDefault whether this bindable is disabled by default.
 */
public fun <T> Bindable(
    value: T,
    disabledByDefault: Boolean,
): Bindable<T> = BindableImpl(value, disabledByDefault)
