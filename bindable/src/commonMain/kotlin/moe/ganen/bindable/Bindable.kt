package moe.ganen.bindable

/**
 * An interface which can be bound to other [Bindable]s in order to watch for (and react to) [value] and [disabled]
 * changes.
 */
public interface Bindable<T> {
    /**
     * Binds ourselves to another bindable such that we receive any value limitations of the bindable we bind with.
     * @param them the foreign bindable. This should always be the most permanent of the bind
     */
    public fun bindTo(them: Bindable<T>)

    /**
     * Unbind ourselves from another bindable.
     * @param them the foreign bindable.
     */
    public fun unbindFrom(them: Bindable<T>?)

    /**
     * Unbind all bounded bindable and events from this bindable.
     */
    public fun unbindAll()

    /**
     * Retrieve a new bindable instance weakly bound to the configuration backing.
     * If you are further binding to events of a bindable retrieved using this method, ensure to hold a local reference.
     * @return a weakly bound copy of the specified bindable.
     * @throws IllegalStateException thrown when attempting to instantiate a copy bindable that is not matching the
     * original's type.
     */
    public fun getBoundCopy(): Bindable<T>

    /**
     * Create a new instance of this [Bindable] for use in [getBoundCopy].
     * The returned instance must have match the most derived type of the bindable class this method is implemented on.
     * @return a new [Bindable] instance.
     */
    public fun createInstance(): Bindable<T>

    // region value

    /**
     * An event which raised when [value] has changed.
     */
    public val valueChanged: Event<ValueChangedEvent<T>>

    /**
     * The current value of this bindable.
     */
    public var value: T

    /**
     * Bind an action to [valueChanged] with the option of running the bound action once immediately.
     * @param runOnceImmediately whether the action provided in [onValueChanged] should be run once immediately.
     * @param onValueChanged the action to perform when [value] changes.
     */
    public fun bindValueChanged(
        runOnceImmediately: Boolean = false,
        onValueChanged: EventObserver<ValueChangedEvent<T>>,
    )
    // endregion

    // region disabled

    /**
     * An event which is raised when [disabled] has changed.
     */
    public val disabledChanged: Event<Boolean>

    /**
     * Whether this bindable has been disabled.
     */
    public var disabled: Boolean

    /**
     * Bind an action to [disabledChanged] with the option of running the bound action once immediately.
     * @param runOnceImmediately whether the action provided in [onDisabledChanged] should be run once immediately.
     * @param onDisabledChanged the action to perform when [disabled] changes.
     */
    public fun bindDisabledChanged(
        runOnceImmediately: Boolean = false,
        onDisabledChanged: EventObserver<Boolean>,
    )
    //endregion

    // region leasing

    /**
     * Takes out a mutually exclusive lease on this bindable.
     * During a lease, the bindable will be set to disabled, but changes can still be applied via the [LeasedBindable] returned by this call.
     * You should end a lease by calling [LeasedBindable.returnLease] when done.
     * @param revertValueOnReturn whether the value when [beginLease] was called should be restored when lease ends.
     * @return a leased bindable instance.
     */
    public fun beginLease(revertValueOnReturn: Boolean): LeasedBindable<T>
    // endregion

    public companion object {
        /**
         * Static method which implements [getBoundCopy] for use in final classes.
         * @param source the [Bindable] source.
         * @return the bound copy.
         */
        @Suppress("UNCHECKED_CAST")
        public fun <S, T : Bindable<S>> getBoundCopyImplementation(source: T): T {
            val copy = source.createInstance()

            if (copy::class != source::class) {
                throw IllegalStateException(
                    "Attempted to create a copy of ${source::class.qualifiedName}, but the returned instance type was " +
                        "${copy::class.qualifiedName}.",
                )
            }

            copy.bindTo(source)
            return copy as T
        }
    }
}
