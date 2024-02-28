package moe.ganen.bindable.example

import moe.ganen.bindable.Bindable

fun main() {
    // example of bindable's value changes propagation.
    changesPropagationExample()

    // example of leasing bindable.
    leasedBindableExample()
}

private fun changesPropagationExample() {
    val bindable = Bindable("Cellinia")
    val bindable1 = bindable.getBoundCopy()

    bindable1.bindValueChanged { event ->
        println("bindable1 value changed ${event.oldValue} -> ${event.newValue}")
    }

    bindable.value += " Texas"
}

private fun leasedBindableExample() {
    val x: Bindable<Int> = Bindable(2)
    val leased = x.beginLease(true)
    x.bindValueChanged(true) {
        println("x value changed ${it.oldValue} -> ${it.newValue}")
    }

    leased.bindValueChanged(true) {
        println("leased value changed ${it.oldValue} -> ${it.newValue}")
    }

    leased.value = 1
    leased.returnLease()
}
