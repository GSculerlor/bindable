package moe.ganen.bindable.internal

import moe.ganen.bindable.Bindable
import moe.ganen.bindable.BindableImpl
import moe.ganen.bindable.LeasedBindable

internal class LeasedBindableImpl<T> private constructor() : LeasedBindable<T>, BindableImpl<T>() {
    private lateinit var source: Bindable<T>

    private var revertValueOnReturn: Boolean = false
    private var disabledBeforeLease: Boolean = false
    private var valueBeforeLease: Any? = Unset

    private var hasBeenReturned: Boolean = false

    internal constructor(source: Bindable<T>, revertValueOnReturn: Boolean) : this() {
        bindTo(source)
        this.source = source

        if (revertValueOnReturn) {
            this.revertValueOnReturn = revertValueOnReturn
            valueBeforeLease = value
        }

        disabledBeforeLease = disabled
        disabled = true
    }

    override fun createInstance(): Bindable<T> = LeasedBindableImpl()

    override fun returnLease(): Boolean {
        if (hasBeenReturned) {
            return false
        }

        if (!::source.isInitialized) {
            throw IllegalStateException("Must return from original leased source.")
        }

        unbindAll()
        return true
    }

    override var value: T
        get() = super.value
        set(value) {
            checkValid()
            if (value == this.value) return

            setValue(super.value, value, true)
        }

    override var disabled: Boolean
        get() = super.disabled
        set(value) {
            checkValid()
            if (disabled == this.value) return

            setDisabled(value, true)
        }

    override fun unbindAll() {
        if (::source.isInitialized && !hasBeenReturned) {
            if (revertValueOnReturn) value = Unset.unbox(valueBeforeLease)

            disabled = disabledBeforeLease

            (source as BindableImpl).endLease(this)
            hasBeenReturned = true
        }

        super.unbindAll()
    }

    private fun checkValid() {
        if (hasBeenReturned) {
            throw UnsupportedOperationException("Cannot perform operations on a LeasedBindable that has been returned.")
        }
    }
}
