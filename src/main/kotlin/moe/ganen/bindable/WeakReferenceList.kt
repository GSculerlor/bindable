package moe.ganen.bindable

import java.lang.ref.WeakReference

/**
 * A mutable list that holds list of weak references of [T].
 *
 * @param delegate List that delegates this.
 */
internal class WeakReferenceList<T : Any>(private val delegate: MutableList<WeakReference<T>>) :
    MutableList<WeakReference<T>> by delegate {
    override fun clear() = synchronized(this) {
        delegate.clear()
    }

    override fun contains(element: WeakReference<T>): Boolean = synchronized(this) {
        return delegate.any { it.get()?.equals(element) ?: false }
    }

    override fun remove(element: WeakReference<T>): Boolean = synchronized(this) {
        return delegate.remove(element)
    }

    internal fun forAliveRefs(cleanUpDestroyedRef: Boolean = true, action: (T) -> Unit) = synchronized(this) {
        getAliveRefs(cleanUpDestroyedRef).forEach { it.get()?.let(action) }
    }

    private fun getAliveRefs(cleanUpDestroyedRef: Boolean = true): List<WeakReference<T>> = synchronized(this) {
        if (cleanUpDestroyedRef) delegate.removeAll { it.get() == null }
        delegate.filter { it.get() != null }
    }
}

internal fun <T : Any> mutableWeakListOf(): WeakReferenceList<T> = WeakReferenceList(mutableListOf())
