plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinter)
}

dependencies {
    implementation(project(":bindable"))
}

kotlin {
    jvmToolchain(17)
}
