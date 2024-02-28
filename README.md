# bindable

[![Build Project](https://github.com/GSculerlor/bindable/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/GSculerlor/bindable/actions/workflows/build.yml)

Implementation of [osu!framework](https://github.com/ppy/osu-framework)'s Bindable concept in Kotlin Multiplatform. To
learn more about Bindable and its original use-cases, please refer to osu!framework's [Bindable wiki](https://github.com/ppy/osu-framework/wiki/Bindable-Flow).

bindable supports Android and iOS (more to come).

You may wonder why you need bindable when Kotlin already have powerful observable concept like `Flow`. The answer is
simple, I don't know either. I made this simply because bindable concept is kinda cool to me and that would be cool if
we have similar thing in Kotlin. In short, I made this just for fun and don't really have a real world use-case (at
least for now).

### setup (Gradle)

```kotlin
dependencies {
    implementation("moe.ganen.bindable:bindable:0.0.1")
}
```

### bindable example

Below is the basic usage of bindable. You can also check the usage of this library in the `example` package.

```kotlin
val bindable = Bindable(0)
val bindable1 = bindable.getBoundCopy()

bindable.bindValueChanged { println(it.newValue) }
bindable.value = 100

assertEquals(100, bindable1) // true
```

You can also use it in Java classes.

```java
void example() {
    Bindable<Integer> bindable = BindableExtKt.Bindable(10);
    Bindable<Integer> bindable1 = bindable.getBoundCopy();

    bindable1.bindValueChanged(false, value -> System.out.println(value.getNewValue()));
    bindable.setValue(100);
}
```

### leased bindable example

To read more about leasing concept please read osu!framework
[wiki](https://github.com/ppy/osu-framework/wiki/Bindable-Flow#leasing) about leasing bindable.

```kotlin
val x: Bindable<Int> = Bindable(2) // x.value == 2
val leased = x.beginLease(true)

leased.value = 1 // x.value == 1
leased.returnLease() // x.value == 2
```

### experimenting its usage on Android

I previously mentioned that this library doesn't have any real world use case as you should use any better dependencies
like`Flow`. But that doesn't mean that this library can't be used on real application.

Let say you have a `ViewModel` or `Presenter` that expose a `Bindable` and you want to observe it on your screen (let's
use Compose UI for this example).

```kotlin
class SomeViewModel : ViewModel(someRepository: SomeRepository) {
    val bindable: Bindable<SomeData?> = Bindable(null)

    init {
        coroutineScope.launch {
            someRepository.observable.collectLatest {
                bindable.value = it
            }
        }
    }
}
```

You can observe the `bindable` in your UI by creating a `bindable` on your UI and bind it to exposed `bindable` from
the `viewModel`.

```kotlin
@Composable
fun SomeScreen(viewModelFactory: () -> SomeViewModel) {
    val someViewModel = viewModel(::viewModelFactory)
    val bindableData: Bindable<SomeData?> = remember { Bindable(null) }
    var someData by remember { mutableStateOf(bindableData.value) }

    LaunchedEffect(bindableData) {
        /* in the real usage of bindable, you often need to create a local bindable and bind it to more persistent 
        bindable (in this case is the exposed bindable from viewModel).
         */
        bindableData.bindTo(someViewModel.someData)
        bindableData.bindValueChanged(true) { someData = it.newValue }
    }

    // Composable function that actually use someData.
    SomeDataScreen(someData = someData)
}
```

please note that this is just an experiment and may have some adjustment and improvement in the future.

### license

This library is licensed under the MIT licence. For more information please refer to the
[licence file](https://github.com/GSculerlor/bindable/blob/master/LICENCE). This license is not covering the usage of
osu!framework name or anything related to osu!framework. For more information about osu!framework license, please refer
to the [osu!framework repository](https://github.com/ppy/osu-framework).
