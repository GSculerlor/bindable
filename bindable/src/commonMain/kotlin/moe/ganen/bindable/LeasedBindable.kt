package moe.ganen.bindable

/**
 * An interface that represents a leased bindable.
 */
public interface LeasedBindable<T> : Bindable<T> {
    /**
     * End the lease on the source [Bindable].
     * @return whether the lease was returned by this call. Return false if already returned.
     */
    public fun returnLease(): Boolean
}
