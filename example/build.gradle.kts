plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinter)
}

group = "moe.ganen.bindable.example"
version = "0.0.1"

dependencies {
    implementation(project(":bindable"))
}

kotlin {
    jvmToolchain(17)
}
