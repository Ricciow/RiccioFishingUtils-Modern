plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

val ksp_version: String by project

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:$ksp_version")
}