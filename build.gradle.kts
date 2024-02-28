plugins {
    //trick: for the same plugin versions in all submodules
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kotlinter).apply(false)
}

buildscript {
    dependencies {
        classpath(libs.kotlinx.atomicfu)
    }
}

group = "moe.ganen.bindable"
version = "0.0.1"

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }
}