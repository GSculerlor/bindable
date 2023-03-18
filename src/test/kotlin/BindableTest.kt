import moe.ganen.bindable.Bindable
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BindableTest {
    @Test
    fun `test bindable default value`() {
        val bindable = Bindable(0)

        assertEquals(0, bindable.value)
        assertFalse(bindable.disabled)
    }

    @Test
    fun `test bindable value changes`() {
        val bindable = Bindable(0)
        var value = bindable.value

        bindable.bindValueChanged { value = it.newValue }

        bindable.value = 1
        assertEquals(1, value)

        bindable.value = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, value)
    }

    @Test
    fun `test bindable complex value changes`() {
        data class BindableValue(val prop: Int)

        val bindable = Bindable(BindableValue(0))
        var value = 0

        bindable.bindValueChanged { value = it.newValue.prop }

        bindable.value = BindableValue(1)
        assertEquals(1, value)

        bindable.value = BindableValue(Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, value)
    }

    @Test
    fun `test bindable disabled`() {
        val bindable = Bindable(0, true)
        var value = bindable.value
        var disabled = bindable.disabled

        bindable.apply {
            bindValueChanged { value = it.newValue }
            bindDisabledChanged { disabled = it }
        }

        assertTrue(disabled)
        assertThrows<IllegalStateException> { bindable.value = 1 }

        bindable.disabled = false
        bindable.value = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, value)
        assertFalse(disabled)
    }

    @Test
    fun `test bindable value changes listener run once immediately`() {
        val bindable = Bindable(0)
        var value = Int.MIN_VALUE

        bindable.bindValueChanged(true) { value = it.newValue }
        assertEquals(0, value)
    }

    @Test
    fun `test bindable disabled listener run once immediately`() {
        val bindable = Bindable(0, true)
        var disabled = false

        bindable.bindDisabledChanged(true) { disabled = it }
        assertTrue(disabled)
    }

    @Test
    fun `test bindable bind default value`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)

        bindable2.bindTo(bindable)
        assertEquals(0, bindable2.value)
    }

    @Test
    fun `test bindable bind disabled value`() {
        val bindable = Bindable(0, true)
        val bindable2 = Bindable(1)

        bindable2.bindTo(bindable)
        assertEquals(0, bindable2.value)
        assertTrue(bindable2.disabled)
    }

    @Test
    fun `test bindable bind value change`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)
        var value = bindable.value

        bindable.apply {
            bindTo(bindable2)
            bindValueChanged {
                value = it.newValue
                println(it.newValue)
            }
        }

        bindable2.value = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, value)
    }

    @Test
    fun `test bindable disabled change`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)

        bindable.bindTo(bindable2)
        bindable2.disabled = true

        assertTrue(bindable.disabled)
        assertThrows<IllegalStateException> { bindable.value = Int.MAX_VALUE }
    }

    @Test
    fun `test bindable bind unbind`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)
        var value = bindable.value

        bindable.apply {
            bindTo(bindable2)
            bindValueChanged { value = it.newValue }
        }

        bindable2.value = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, value)

        bindable.unbindFrom(bindable2)

        bindable2.value = Int.MIN_VALUE
        assertEquals(Int.MAX_VALUE, value)
        assertEquals(Int.MIN_VALUE, bindable2.value)
    }

    @Test
    fun `test bindable bind unbind from the other bindable`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)
        var value = bindable.value

        bindable.apply {
            bindTo(bindable2)
            bindValueChanged { value = it.newValue }
        }

        bindable2.value = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, value)

        bindable.unbindFrom(bindable2)

        bindable2.value = Int.MIN_VALUE
        assertEquals(Int.MAX_VALUE, value)
        assertEquals(Int.MIN_VALUE, bindable2.value)

        bindable.value = 0
        assertEquals(0, value)
        assertEquals(Int.MIN_VALUE, bindable2.value)
    }

    @Test
    fun `test bindable bind to itself`() {
        val bindable = Bindable(0)

        assertThrows<IllegalStateException> { bindable.bindTo(bindable) }
    }

    @Test
    fun `test bindable duplicate bind`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)

        bindable2.bindTo(bindable)

        assertThrows<IllegalStateException> { bindable2.bindTo(bindable) }
        assertThrows<IllegalStateException> { bindable.bindTo(bindable2) }
    }

    @Test
    fun `test bindable unbind bindings`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)
        val bindable3 = Bindable(2)
        var value = bindable.value

        bindable.apply {
            bindValueChanged { value = it.newValue }
            bindTo(bindable2, bindable3)
        }
        assertEquals(2, value)

        bindable2.value = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, value)

        bindable.unbindBindings()
        bindable2.value = Int.MIN_VALUE
        assertEquals(Int.MAX_VALUE, value)
        assertEquals(Int.MIN_VALUE, bindable2.value)
    }

    @Test
    fun `test bindable bind nullable`() {
        val bindable = Bindable<Int?>(null)
        val bindable2 = Bindable<Int?>(1)

        bindable2.bindTo(bindable)
        assertNull(bindable2.value)
    }

    @Test
    fun `test bindable trigger change`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(1)

        // probably better to verify invocation count instead?
        var value = 0
        var value2 = 0

        bindable2.bindValueChanged { value2++ }
        bindable.apply {
            bindValueChanged { value++ }
            bindTo(bindable2)

            triggerChange()
        }

        assertEquals(2, value)
        assertEquals(0, value2)

        bindable2.triggerChange()
        assertEquals(1, value2)
    }

    @Test
    fun `test bindable unbind event`() {
        val bindable = Bindable(0)
        var value = bindable.value
        var disabledValue = bindable.disabled

        bindable.apply {
            bindValueChanged { value = it.newValue }
            bindDisabledChanged { disabledValue = it }
        }

        bindable.value = 1
        assertEquals(1, value)

        bindable.disabled = true
        assertTrue(disabledValue)

        bindable.apply {
            unbindEvents()
            disabled = false
            this.value = 2
        }
        assertTrue(disabledValue)
        assertEquals(1, value)
    }

    @Test
    fun `test bindable unbind all`() {
        val bindable = Bindable(0)
        val bindable2 = Bindable(0)

        var value = bindable.value
        var disabledValue = bindable.disabled

        bindable.apply {
            bindValueChanged { value = it.newValue }
            bindDisabledChanged { disabledValue = it }

            bindTo(bindable2)
        }

        bindable2.value = 1
        assertEquals(1, value)

        bindable2.disabled = true
        assertTrue(disabledValue)

        bindable2.apply {
            unbindAll()
            disabled = false
            this.value = 2
        }
        assertTrue(disabledValue)
        assertEquals(1, value)
    }
}
