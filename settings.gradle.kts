pluginManagement {
    val kotlinVersion = "1.5.21"
    val kspVersion = "1.5.21-1.0.0-beta05"
    val agpVersion = "7.1.0-alpha03"
    plugins {
        id("com.google.devtools.ksp") version kspVersion apply false
        kotlin("jvm") version kotlinVersion apply false
        kotlin("android") version kotlinVersion apply false
        id("com.android.application") version agpVersion apply false
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "hello-ksp"

include(":app")
include(":processor")
