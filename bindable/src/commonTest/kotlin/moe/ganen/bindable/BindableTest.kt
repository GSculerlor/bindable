package moe.ganen.bindable

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BindableTest {
    @Test
    fun `test constructor value uses as default value and initial value`() {
        val value = "Cellinia Texas"
        val bindable = Bindable(value)
        assertEquals(value, bindable.value)
    }

    @Test
    fun `test constructor disable uses as disable value`() {
        val bindable = Bindable("Cellinia Texas", true)
        assertTrue(bindable.disabled)
    }

    @Test
    fun `test bind to already bound bindable will throw exception`() {
        val bindable1 = Bindable("Cellinia Texas")
        val bindable2 = bindable1.getBoundCopy()
        val exception = assertFailsWith(IllegalStateException::class) { bindable2.bindTo(bindable1) }
        assertEquals("An already bound bindable cannot be bound again.", exception.message)
    }

    @Test
    fun `test bound bindables value change propagation`() {
        val defaultValue = "Cellinia Texas"

        val bindable1 = Bindable(defaultValue)
        val bindable2 = bindable1.getBoundCopy()
        val bindable3 = bindable2.getBoundCopy()

        assertEquals(defaultValue, bindable1.value)
        assertEquals(bindable1.value, bindable2.value)
        assertEquals(bindable1.value, bindable3.value)

        val newValue = "Texas"
        bindable1.value = newValue

        assertEquals(newValue, bindable1.value)
        assertEquals(bindable1.value, bindable2.value)
        assertEquals(bindable1.value, bindable3.value)
    }

    @Test
    fun `test bound copy bindables have same disabled state`() {
        val bindable1 = Bindable("Cellinia Texas", true)
        val bindable2 = bindable1.getBoundCopy()
        val bindable3 = bindable2.getBoundCopy()

        assertTrue(bindable2.disabled)
        assertTrue(bindable3.disabled)
    }

    @Test
    fun `test change disabled bindable value will throw exception`() {
        val bindable1 = Bindable("Cellinia Texas", true)
        val exception = assertFailsWith(UnsupportedOperationException::class) { bindable1.value = "Texas" }
        assertEquals("Can not set value to Texas as bindable is disabled.", exception.message)
    }

    @Test
    fun `test change disabled bindable state then change value`() {
        val newValue = "Texas"

        val bindable =
            Bindable("Cellinia Texas", true).apply {
                bindDisabledChanged { assertFalse(it) }
                bindValueChanged { assertEquals(newValue, it.newValue) }
            }

        bindable.disabled = false
        bindable.value = newValue
    }

    @Test
    fun `test bind multiple bindable via extension function`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)
        val bindable3 = Bindable(2)

        bindable.bindTo(bindable2, bindable3)
        assertEquals(0, bindable.value)
        assertEquals(bindable.value, bindable2.value)
        assertEquals(bindable.value, bindable3.value)
    }
}
