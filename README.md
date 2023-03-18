# bindable
a Kotlin implementation of [osu!framework](https://github.com/ppy/osu-framework)'s Bindable concept.
### example
```kotlin
val bindable = Bindable(0)
val bindable2 = Bindable(1)

bindable.apply {
    bindValueChanged { println(it.newValue) }
    bindTo(bindable2)
}

bindable2.value = Int.MAX_VALUE
bindable.value = 100
```
output:
```markdown
1
2147483647
100
```

### installation
todo