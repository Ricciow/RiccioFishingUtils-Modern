plugins {
    id("fabric-loom")
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava = when {
    stonecutter.eval(stonecutter.current.version, ">=1.20.6") -> JavaVersion.VERSION_21
    stonecutter.eval(stonecutter.current.version, ">=1.18") -> JavaVersion.VERSION_17
    stonecutter.eval(stonecutter.current.version, ">=1.17") -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")

    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        name = "DevAuth"
    }

    maven(url = "https://repo.essential.gg/repository/maven-public") {
        name = "Elementa"
    }

    maven(url = "https://maven.teamresourceful.com/repository/maven-public/") {
        name = "Resourceful Config"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")

    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_language_kotlin")}")
    modImplementation(include("gg.essential:universalcraft-${property("universalcraft_mc_version")}-fabric:${property("universalcraft_version")}")!!)
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-${property("resourceful_mc_version")}:${property("resourceful_version")}")!!)
    modImplementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-${property("resourcefulkt_mc_version")}:${property("resourcefulkt_version")}")!!)

    implementation(include("gg.essential:elementa:${property("elementa_version")}")!!)

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${property("devauth_version")}")

    ksp(project(":processor"))

}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/rfu.accesswidener")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

kotlin {
    jvmToolchain(requiredJava.majorVersion.toInt())

    sourceSets {
        main {
            kotlin.srcDir("build/generated/ksp/main/kotlin")
        }
    }
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.version)
        inputs.property("minecraft", project.property("mod.mc_dep"))
        inputs.property("fabric_language_kotlin", project.property("fabric_language_kotlin"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.version,
            "minecraft" to project.property("mod.mc_dep"),
            "fabric_language_kotlin" to project.property("fabric_language_kotlin")
        )

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    clean {
        delete(rootProject.layout.buildDirectory.file("libs/${base.archivesName.get()}-${version}.jar"))
    }

    remapJar {
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}