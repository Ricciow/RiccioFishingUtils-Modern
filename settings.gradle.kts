pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }

    val kotlinVersion = providers.gradleProperty("kotlin_version").get()
    val loomVersion = providers.gradleProperty("loom_version").get()
    val kspVersion = providers.gradleProperty("ksp_version").get()

    plugins {
        id("fabric-loom") version loomVersion
        id("net.fabricmc.fabric-loom") version loomVersion
        kotlin("jvm") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
    }

}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    create(rootProject) {
        versions("26.1", "26.1.1", "26.1.2", "26.2", "26.3")
        vcsVersion = "26.3"
    }
}

rootProject.name = "RiccioFishingUtils"
include(":processor")