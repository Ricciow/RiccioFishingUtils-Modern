pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }

    val kotlin_version: String by settings
    val loom_version: String by settings

    plugins {
        id("fabric-loom") version loom_version
        kotlin("jvm") version kotlin_version
    }

}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.11"
}

stonecutter {
    create(rootProject) {
        versions("1.21.5", "1.21.8", "1.21.10")
        vcsVersion = "1.21.10"
    }
}

rootProject.name = "Template"