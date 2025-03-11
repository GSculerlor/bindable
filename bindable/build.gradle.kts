import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kotlinx.binaryCompatibilityValidator)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin {
    jvmToolchain(17)

    explicitApi()
    jvm()
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
    macosX64()
    macosArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        watchosArm32(),
        watchosArm64(),
        watchosSimulatorArm64(),
        tvosArm64(),
        tvosX64(),
        tvosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "library"
        }
    }
    applyDefaultHierarchyTemplate()


    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "moe.ganen.bindable"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
    }
}

apiValidation {
    ignoredClasses.add("moe.ganen.bindable.WeakReference")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.addAll(
        "-Xexpect-actual-classes",
        "-opt-in=kotlin.experimental.ExperimentalNativeApi"
    )
}

mavenPublishing {
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Dokka("dokkaHtml")))
    coordinates("moe.ganen.bindable", "bindable", "1.0.0-SNAPSHOT")

    pom {
        name.set("bindable")
        description.set("Kotlin implementation of osu!framework's Bindable concept.")
        url.set("https://github.com/GSculerlor/bindable")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/GSculerlor/bindable/blob/master/LICENSE.txt")
            }
        }
        scm {
            connection.set("https://github.com/GSculerlor/bindable.git")
            developerConnection.set("https://github.com/GSculerlor/bindable.git")
            url.set("https://github.com/GSculerlor/bindable")
        }
        developers {
            developer {
                name.set("Ganendra Afrasya")
                url.set("https://github.com/GSculerlor")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "githubPackage"
            url = uri("https://maven.pkg.github.com/GSculerlor/bindable")
            credentials(PasswordCredentials::class)
        }
    }
}

// TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
    dependsOn(project.tasks.withType(Sign::class.java))
}
