package moe.ganen.bindable

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LeasedBindableTest {
    @Test
    fun `test leasing bindable and return it`() {
        val bindable = Bindable(1)
        val leasedBindable = bindable.beginLease(true)
        assertEquals(bindable.value, leasedBindable.value)

        leasedBindable.value = 2
        assertEquals(2, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)

        leasedBindable.returnLease()
        assertEquals(1, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)
    }

    @Test
    fun `test leasing bindable and return it without revert value`() {
        val bindable = Bindable(1)
        val leasedBindable = bindable.beginLease(false)
        assertEquals(bindable.value, leasedBindable.value)

        leasedBindable.value = 2
        assertEquals(2, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)

        leasedBindable.returnLease()
        assertEquals(2, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)
    }

    @Test
    fun `test leasing bindable and return it with revert value`() {
        val bindable = Bindable(1)
        val leasedBindable = bindable.beginLease(true)
        assertTrue(bindable.disabled)
        assertEquals(bindable.disabled, leasedBindable.disabled)

        leasedBindable.disabled = true
        assertTrue(bindable.disabled)
        assertEquals(bindable.disabled, leasedBindable.disabled)

        leasedBindable.returnLease()
        assertFalse(bindable.disabled)
        assertEquals(bindable.disabled, leasedBindable.disabled)
    }

    @Test
    fun `test leasing bindable and return it without revert disable value`() {
        val bindable = Bindable(1, true)
        val leasedBindable = bindable.beginLease(false)
        assertTrue(bindable.disabled)
        assertEquals(bindable.disabled, leasedBindable.disabled)

        leasedBindable.disabled = true
        assertTrue(bindable.disabled)
        assertEquals(bindable.disabled, leasedBindable.disabled)

        leasedBindable.returnLease()
        assertTrue(bindable.disabled)
        assertTrue(leasedBindable.disabled)

        val bindable2 = Bindable(1)
        val leasedBindable2 = bindable2.beginLease(false)
        assertTrue(bindable2.disabled)
        assertEquals(bindable2.disabled, leasedBindable2.disabled)

        leasedBindable2.disabled = true
        assertTrue(bindable2.disabled)
        assertEquals(bindable2.disabled, leasedBindable2.disabled)

        leasedBindable2.returnLease()
        assertFalse(bindable2.disabled)
        assertFalse(leasedBindable2.disabled)
    }

    @Test
    fun `test leasing bindable and return it via unbind`() {
        val bindable = Bindable(1)
        val leasedBindable = bindable.beginLease(true)
        assertEquals(bindable.value, leasedBindable.value)

        leasedBindable.value = 2
        assertEquals(2, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)

        bindable.unbindAll()

        assertEquals(1, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)
    }

    @Test
    fun `test leasing bindable and return it via unbind without revert value`() {
        val bindable = Bindable(1)
        val leasedBindable = bindable.beginLease(false)
        assertEquals(bindable.value, leasedBindable.value)

        leasedBindable.value = 2
        assertEquals(2, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)

        bindable.unbindAll()

        assertEquals(2, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)
    }

    @Test
    fun `test consecutive leasing`() {
        val bindable = Bindable(1)

        val leasedBindable1 = bindable.beginLease(false)
        leasedBindable1.returnLease()
        val leased2 = bindable.beginLease(false)
        leased2.returnLease()
    }

    @Test
    fun `test leasing a leasedBindable bindable will throw exception`() {
        val bindable = Bindable(1)
        bindable.beginLease(true)

        val exception = assertFailsWith(UnsupportedOperationException::class) { bindable.beginLease(true) }
        assertEquals("Attempted to lease a bindable that is already in a leased state.", exception.message)
    }

    @Test
    fun `test end leasing bindable from different source`() {
        val bindable = Bindable(1)
        val bindable2 = Bindable(0)
        val leasedBindable = bindable2.beginLease(true)

        bindable.beginLease(true)
        val exception2 =
            assertFailsWith(UnsupportedOperationException::class) { (bindable as BindableImpl).endLease(leasedBindable) }
        assertEquals(
            "Attempted to end a lease but returned a different bindable to the one used to start the lease.",
            exception2.message,
        )
    }

    @Test
    fun `test end leasing unleased bindable`() {
        val bindable = Bindable(1)
        val leasedBindable = Bindable(0).beginLease(true)

        val exception =
            assertFailsWith(UnsupportedOperationException::class) { (bindable as BindableImpl).endLease(leasedBindable) }
        assertEquals("Attempted to end a lease without beginning one.", exception.message)
    }

    @Test
    fun `test changing value after returning lease`() {
        val bindable = Bindable(1)
        val leasedBindable = bindable.beginLease(false)
        leasedBindable.returnLease()

        val exception = assertFailsWith(UnsupportedOperationException::class) { leasedBindable.value = 2 }
        assertEquals("Cannot perform operations on a LeasedBindable that has been returned.", exception.message)
    }

    @Test
    fun `test changing disabled after returning lease`() {
        val bindable = Bindable(1)
        val leasedBindable = bindable.beginLease(false)
        leasedBindable.returnLease()

        val exception = assertFailsWith(UnsupportedOperationException::class) { leasedBindable.disabled = true }
        assertEquals("Cannot perform operations on a LeasedBindable that has been returned.", exception.message)
    }

    @Test
    fun `test lease bound copy of bindable then original bindable also behaves as leased`() {
        val bindable = Bindable(0)
        val bindableCopy = bindable.getBoundCopy()
        val leasedBindable = bindableCopy.beginLease(true)

        val exception = assertFailsWith(UnsupportedOperationException::class) { bindable.beginLease(false) }
        assertEquals("Attempted to lease a bindable that is already in a leased state.", exception.message)

        val exception2 = assertFailsWith(UnsupportedOperationException::class) { bindableCopy.beginLease(false) }
        assertEquals("Attempted to lease a bindable that is already in a leased state.", exception2.message)

        leasedBindable.value = 2
        assertEquals(2, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)
        assertEquals(bindable.value, bindableCopy.value)
    }

    @Test
    fun `test bound copy of leased bindable should propagate changes to its original`() {
        val bindable = Bindable(0)
        val bindableCopy = bindable.getBoundCopy()

        val leasedBindable = bindableCopy.beginLease(true)
        val leasedCopy = leasedBindable.getBoundCopy()

        leasedCopy.value = 3
        assertEquals(3, bindable.value)
        assertEquals(bindable.value, leasedBindable.value)
        assertEquals(bindable.value, bindableCopy.value)
    }
}
