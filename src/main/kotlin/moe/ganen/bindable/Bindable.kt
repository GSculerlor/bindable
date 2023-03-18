package moe.ganen.bindable

import java.lang.ref.WeakReference

/**
 * An implementation of osu!framework's Bindable. Bindable is an object that can be bound to other [Bindable]s in order
 * to watch and react to [value] and [disabled] changes.
 *
 * @param initialValue The initial value of this [Bindable]
 * @param disabledByDefault Whether the [Bindable] is disabled when initialized.
 *
 * @see <a href="https://github.com/ppy/osu-framework/">osu!framework</a>
 * @see <a href="https://github.com/ppy/osu-framework/tree/master/osu.Framework/Bindables">osu!framework's bindable</a>
 */
class Bindable<T>(initialValue: T, disabledByDefault: Boolean = false) {
    private val bindings: WeakReferenceList<Bindable<T>> = mutableWeakListOf()
    private val weakReference: WeakReference<Bindable<T>> by lazy { WeakReference(this) }

    private var _value: T = initialValue
    private val valueChanged: Event<ValueChangedEvent<T>> = Event()

    private var _disabled: Boolean = disabledByDefault
    private val disabledChanged: Event<Boolean> = Event()

    /**
     * Current value of this [Bindable].
     */
    var value: T
        get() = _value
        set(value) {
            if (disabled) throw IllegalStateException("Trying to update value when bindable is disabled")

            if (value?.equals(_value) == true) return
            setValue(_value, value)
        }

    /**
     * Bind a function to [valueChanged] that invoked when [value] has changed.
     *
     * @param runOnceImmediately Whether the action should be invoked once immediately.
     * @param onChange The action to invoke when [value] changes.
     */
    fun bindValueChanged(runOnceImmediately: Boolean = false, onChange: (ValueChangedEvent<T>) -> Unit) {
        valueChanged += onChange
        if (runOnceImmediately) {
            onChange(ValueChangedEvent(_value, _value))
        }
    }

    private fun setValue(previousValue: T, value: T, source: Bindable<T>? = null) {
        _value = value
        triggerValueChange(previousValue, source ?: this)
    }

    private fun triggerValueChange(previousValue: T, source: Bindable<T>, propagateToBindings: Boolean = true) {
        val beforePropagation = _value
        if (propagateToBindings && bindings.isNotEmpty()) {
            bindings.forAliveRefs {
                if (it == source) return@forAliveRefs
                it.setValue(previousValue, _value, this)
            }
        }

        if (beforePropagation?.equals(_value) == true) {
            valueChanged(ValueChangedEvent(previousValue, value))
        }
    }

    /**
     * Whether this bindable is disabled. When this bindable is disabled, attempting to update the [value] will throw [IllegalStateException].
     */
    var disabled: Boolean
        get() = _disabled
        set(value) {
            if (value == _disabled) return

            setDisabled(value)
        }

    /**
     * Bind a function to [disabledChanged] that invoked when [disabled] has changed.
     *
     * @param runOnceImmediately Whether the action should be invoked once immediately.
     * @param onChange The action to invoke when [disabled] changes.
     */
    fun bindDisabledChanged(runOnceImmediately: Boolean = false, onChange: (Boolean) -> Unit) {
        disabledChanged += onChange
        if (runOnceImmediately) {
            onChange(_disabled)
        }
    }

    private fun setDisabled(disabled: Boolean, source: Bindable<T>? = null) {
        _disabled = disabled
        triggerDisabledChange(source ?: this)
    }

    private fun triggerDisabledChange(source: Bindable<T>, propagateToBindings: Boolean = true) {
        val beforePropagation = _disabled
        if (propagateToBindings && bindings.isNotEmpty()) {
            bindings.forAliveRefs {
                if (it == source) return@forAliveRefs
                it.setDisabled(_disabled, this)
            }
        }

        if (beforePropagation == _disabled) {
            disabledChanged(_disabled)
        }
    }

    /**
     * Invoke [triggerValueChange] and [triggerDisabledChange] once without actual [value] or [disabled] value changes.
     * Invocation is not propagated to bindings.
     */
    fun triggerChange() {
        triggerValueChange(_value, this, false)
        triggerDisabledChange(this, false)
    }

    /**
     * Binds this bindable to another [Bindable]s such that bidirectional updates are propagated.
     *
     * @param bindable The other [Bindable]s that will be bound to this bindable.
     * @throws IllegalStateException When trying to bind to itself or trying to bind bindable that already bound to this bindable.
     */
    fun bindTo(vararg bindable: Bindable<T>) {
        bindable.forEach { bindTo(it) }
    }

    /**
     * Binds this bindable to another [Bindable] such that bidirectional updates are propagated.
     *
     * @param bindable The other [Bindable] that will be bound to this bindable.
     * @throws IllegalStateException When trying to bind to itself or trying to bind bindable that already bound to this bindable.
     */
    fun bindTo(bindable: Bindable<T>) {
        if (bindable === this) throw IllegalStateException("Trying to bind to itself")

        if (bindings.any { it.get()?.equals(bindable) == true }) {
            throw IllegalStateException("Trying to bind bindable that already bound to it")
        }

        value = bindable.value
        disabled = bindable.disabled

        addBinding(bindable.weakReference)
        bindable.addBinding(weakReference)
    }

    /**
     * Unbinds this bindable from another [Bindable] such that this bindable will stop getting update from that bindable.
     * Note that this applied bidirectional meaning that the other bindable will also stop receiving updates.
     *
     * @param bindable The other [Bindable].
     */
    fun unbindFrom(bindable: Bindable<T>) {
        removeBinding(bindable.weakReference)
        bindable.removeBinding(weakReference)
    }

    /**
     * Unbinds all action bound to this [Bindable].
     */
    fun unbindEvents() {
        valueChanged.clear()
        disabledChanged.clear()
    }

    /**
     * Unbinds all bound [Bindable]s.
     */
    fun unbindBindings() {
        if (bindings.isNotEmpty()) {
            bindings.forAliveRefs { it.unbindFrom(this) }
            bindings.clear()
        }
    }

    /**
     * Unbinds all events and bound [Bindable]s.
     */
    fun unbindAll() {
        unbindEvents()
        unbindBindings()
    }

    private fun addBinding(ref: WeakReference<Bindable<T>>) {
        bindings.add(ref)
    }

    private fun removeBinding(ref: WeakReference<Bindable<T>>) {
        bindings.remove(ref)
    }
}
