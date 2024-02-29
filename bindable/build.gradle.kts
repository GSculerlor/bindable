plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.dokka)
    id("kotlinx-atomicfu")
    id("maven-publish")
}

version = rootProject.version
group = rootProject.group

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
        val commonMain by getting
        val jvmMain by getting
        val androidMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val jvmTest by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

android {
    namespace = "moe.ganen.bindable"
    compileSdk = 33
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

fun MavenPublication.mavenCentralPom() {
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
    if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        // already has publications, just need to add javadoc task
        val javadocJar by tasks.creating(Jar::class) {
            from("javadoc")
            archiveClassifier.set("javadoc")
        }
        publications.all {
            if (this is MavenPublication) {
                artifact(javadocJar)
                mavenCentralPom()
            }
        }
        // create task to publish all apple (macos, ios, tvos, watchos) artifacts
        val publishApple by tasks.registering {
            publications.all {
                if (name.contains(Regex("macos|ios|tvos|watchos"))) {
                    val publicationNameForTask = name.replaceFirstChar(Char::uppercase)
                    dependsOn("publish${publicationNameForTask}PublicationToSonatypeRepository")
                }
            }
        }
    } else {
        // Need to create source, javadoc & publication
        val java = extensions.getByType<JavaPluginExtension>()
        java.withSourcesJar()
        java.withJavadocJar()
        publications {
            create<MavenPublication>("lib") {
                from(components["java"])
                mavenCentralPom()
            }
        }
    }
}

// TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
    dependsOn(project.tasks.withType(Sign::class.java))
}
