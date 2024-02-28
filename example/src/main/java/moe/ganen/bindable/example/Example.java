package moe.ganen.bindable.example;

import moe.ganen.bindable.Bindable;
import moe.ganen.bindable.BindableExtKt;

public class Example {
    public static void main(String[] args) {
        Bindable<Integer> bindable = BindableExtKt.Bindable(10);
        Bindable<Integer> bindableCopy = bindable.getBoundCopy();

        bindable.bindValueChanged(true, value -> {
            System.out.println(value.getNewValue());
        });

        bindableCopy.setValue(100);
    }
}
