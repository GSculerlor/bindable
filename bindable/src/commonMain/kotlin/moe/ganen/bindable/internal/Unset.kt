package moe.ganen.bindable.internal

import moe.ganen.bindable.BindableImpl

/**
 * Data object to box the initial value of [BindableImpl].
 * We might want to throw exception when the value equal this when unboxed but need more consideration whether it was
 * worth it or not.
 */
internal data object Unset {
    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    inline fun <T> unbox(value: Any?): T =
        if (value === this) {
            null as T
        } else {
            value as T
        }
}
