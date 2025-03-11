package moe.ganen.bindable.internal

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import moe.ganen.bindable.WeakReference

/**
 * A mutable list that holds list of weak references of [T].
 *
 * @param delegate List that delegates this.
 */
internal class WeakReferenceList<T : Any>(private val delegate: MutableList<WeakReference<T>>) :
    MutableList<WeakReference<T>> by delegate {
    private val lock = SynchronizedObject()

    override fun clear(): Unit =
        synchronized(lock) {
            delegate.clear()
        }

    override fun contains(element: WeakReference<T>): Boolean =
        synchronized(lock) {
            return delegate.any { it.get()?.equals(element) ?: false }
        }

    override fun remove(element: WeakReference<T>): Boolean =
        synchronized(lock) {
            return delegate.remove(element)
        }

    override val size: Int
        get() = synchronized(lock) { delegate.size }

    internal fun forAliveRefs(
        cleanUpDestroyedRef: Boolean = true,
        action: (T) -> Unit,
    ) = synchronized(lock) {
        getAliveRefs(cleanUpDestroyedRef).forEach { it.get()?.let(action) }
    }

    private fun getAliveRefs(cleanUpDestroyedRef: Boolean = true): List<WeakReference<T>> =
        synchronized(lock) {
            if (cleanUpDestroyedRef) delegate.removeAll { it.get() == null }
            delegate.filter { it.get() != null }
        }
}

internal fun <T : Any> mutableWeakListOf(): WeakReferenceList<T> = WeakReferenceList(mutableListOf())
