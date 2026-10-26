pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // 1.21.6 is the first version with Fabric's HudElementRegistry and the current GUI renderer.
        versions("1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11")
        vcsVersion = "1.21.11"
    }
}

rootProject.name = "voltliz-client"
