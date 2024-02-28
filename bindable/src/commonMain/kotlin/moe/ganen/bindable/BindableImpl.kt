package moe.ganen.bindable

import moe.ganen.bindable.internal.LeasedBindableImpl
import moe.ganen.bindable.internal.Unset
import moe.ganen.bindable.internal.WeakReferenceList
import moe.ganen.bindable.internal.mutableWeakListOf

/**
 * Generic implementation of [Bindable].
 */
internal open class BindableImpl<T>() : Bindable<T> {
    private val weakReference: WeakReference<Bindable<T>> by lazy { WeakReference(this) }
    private val bindings: WeakReferenceList<Bindable<T>> = mutableWeakListOf()

    /**
     * Create new [BindableImpl] with initial value. This bindable will not be disabled by default.
     * @param value initial value of the bindable.
     */
    constructor(value: T) : this() {
        this.value = value
    }

    /**
     * Create new [BindableImpl] with initial value and disabled state.
     * @param value initial value of the bindable.
     * @param disabledByDefault whether this bindable is disabled by default.
     */
    constructor(value: T, disabledByDefault: Boolean) : this(value) {
        disabled = disabledByDefault
    }

    override fun bindTo(them: Bindable<T>) {
        if (bindings.any { it.get()?.equals(them) == true }) {
            throw IllegalStateException("An already bound bindable cannot be bound again.")
        }

        value = them.value
        disabled = them.disabled

        addWeakReference((them as BindableImpl).weakReference)
        them.addWeakReference(weakReference)
    }

    override fun getBoundCopy(): Bindable<T> = Bindable.getBoundCopyImplementation(this)

    override fun createInstance(): Bindable<T> = BindableImpl()

    // region value
    override val valueChanged: Event<ValueChangedEvent<T>> = Event()

    private var _value: Any? = Unset

    override var value: T
        get() = Unset.unbox(_value)
        set(value) {
            if (disabled) throw UnsupportedOperationException("Can not set value to $value as bindable is disabled.")

            if (value == Unset.unbox(_value)) return

            @Suppress("UNCHECKED_CAST")
            setValue(_value as T, value)
        }

    override fun bindValueChanged(
        runOnceImmediately: Boolean,
        onValueChanged: EventObserver<ValueChangedEvent<T>>,
    ) {
        valueChanged += onValueChanged
        if (runOnceImmediately) {
            val sentValue: T = Unset.unbox(_value)
            onValueChanged.observe(ValueChangedEvent(sentValue, sentValue))
        }
    }

    protected fun setValue(
        previousValue: T,
        value: T,
        bypassCheck: Boolean = false,
        source: Bindable<T>? = null,
    ) {
        _value = value
        triggerValueChange(previousValue, source ?: this, bypassCheck = bypassCheck)
    }

    @Suppress("UNCHECKED_CAST")
    private fun triggerValueChange(
        previousValue: T,
        source: Bindable<T>,
        propagateToBindings: Boolean = true,
        bypassCheck: Boolean = false,
    ) {
        val beforePropagation = _value as T
        if (propagateToBindings && bindings.isNotEmpty()) {
            bindings.forAliveRefs {
                if (it == source) return@forAliveRefs
                (it as BindableImpl).setValue(previousValue, beforePropagation, bypassCheck, this)
            }
        }

        if (beforePropagation == _value) {
            valueChanged(ValueChangedEvent(previousValue, value))
        }
    }
    // endregion

    // region disabled
    override val disabledChanged: Event<Boolean> = Event()

    private var _disabled: Boolean = false

    override var disabled: Boolean
        get() = _disabled
        set(value) {
            throwIfLeased()

            if (value == _disabled) return
            setDisabled(value)
        }

    override fun bindDisabledChanged(
        runOnceImmediately: Boolean,
        onDisabledChanged: EventObserver<Boolean>,
    ) {
        disabledChanged += onDisabledChanged
        if (runOnceImmediately) {
            onDisabledChanged.observe(_disabled)
        }
    }

    protected fun setDisabled(
        disabled: Boolean,
        bypassCheck: Boolean = false,
        source: Bindable<T>? = null,
    ) {
        if (!bypassCheck) throwIfLeased()

        _disabled = disabled
        triggerDisabledChange(source ?: this, bypassCheck = bypassCheck)
    }

    private fun triggerDisabledChange(
        source: Bindable<T>,
        propagateToBindings: Boolean = true,
        bypassCheck: Boolean = false,
    ) {
        val beforePropagation = _disabled
        if (propagateToBindings && bindings.isNotEmpty()) {
            bindings.forAliveRefs {
                if (it == source) return@forAliveRefs
                (it as BindableImpl).setDisabled(_disabled, bypassCheck, this)
            }
        }

        if (beforePropagation == _disabled) {
            disabledChanged(_disabled)
        }
    }
    // endregion

    // region leasing
    private var leasedBindable: LeasedBindable<T>? = null

    private val isLeased: Boolean
        get() = leasedBindable != null

    override fun beginLease(revertValueOnReturn: Boolean): LeasedBindable<T> {
        if (checkForLease(this)) {
            throw UnsupportedOperationException("Attempted to lease a bindable that is already in a leased state.")
        }

        val leased = LeasedBindableImpl(this, revertValueOnReturn)
        leasedBindable = leased

        return leased
    }

    internal fun endLease(returnedBindable: LeasedBindable<T>) {
        if (!isLeased) throw UnsupportedOperationException("Attempted to end a lease without beginning one.")
        if (returnedBindable != leasedBindable) {
            throw UnsupportedOperationException(
                "Attempted to end a lease but returned a different bindable to the one used to start the lease.",
            )
        }

        leasedBindable = null
    }

    private fun checkForLease(source: Bindable<T>): Boolean {
        if (isLeased) return true
        if (bindings.isEmpty()) return false

        var found = false
        for (binding in bindings) {
            if (binding.get() != source) {
                found = found or (binding.get() as BindableImpl).checkForLease(this)
            }
        }

        return found
    }
    // endregion

    override fun unbindAll() {
        if (isLeased) leasedBindable?.returnLease()

        unbindEvents()
        unbindBindings()
    }

    private fun unbindEvents() {
        valueChanged.clear()
        disabledChanged.clear()
    }

    private fun unbindBindings() {
        if (bindings.isEmpty()) return

        for (binding in bindings.toTypedArray()) {
            unbindFrom(binding.get())
        }
    }

    override fun unbindFrom(them: Bindable<T>?) {
        if (them == null) {
            throw NullPointerException("Trying to unbind from null bindable.")
        }

        if (them !is BindableImpl<T>) {
            throw UnsupportedOperationException(
                "Can't unbind a bindable of type ${them::class.qualifiedName} from a bindable of type ${this::class.qualifiedName}.",
            )
        }

        removeWeakReference(them.weakReference)
        them.removeWeakReference(weakReference)
    }

    private fun addWeakReference(ref: WeakReference<Bindable<T>>) {
        bindings.add(ref)
    }

    private fun removeWeakReference(ref: WeakReference<Bindable<T>>) {
        bindings.remove(ref)
    }

    private fun throwIfLeased() {
        if (isLeased) {
            throw UnsupportedOperationException("Cannot perform this operation on a Bindable that is currently in a leased state.")
        }
    }
}
