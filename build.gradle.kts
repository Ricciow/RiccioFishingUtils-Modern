plugins {
    id("fabric-loom")
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("com.modrinth.minotaur") version "2.8.7"
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

if (project.name != rootProject.name && project.name != "processor") {
    modrinth {
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set(System.getenv("MODRINTH_PROJECT_ID"))
        versionNumber.set(project.version.toString())
        versionType.set("release")
        uploadFile.set(tasks.remapJar)
        gameVersions.add(stonecutter.current.version)
        loaders.add("fabric")

        // The changelog is handled in the workflow to avoid complex parsing in Gradle
        changelog.set(System.getenv("CHANGELOG_BODY"))

        dependencies {
            required.project("fabric-api")
            required.project("fabric-language-kotlin")
            required.project("hypixel-mod-api")
        }

        syncBodyFrom.set(rootProject.file("README.md").readText())
    }
} else {
    tasks.named("modrinth") {
        enabled = false
    }
}

val requiredJava = when {
    stonecutter.eval(stonecutter.current.version, ">=1.20.6") -> JavaVersion.VERSION_21
    stonecutter.eval(stonecutter.current.version, ">=1.18") -> JavaVersion.VERSION_17
    stonecutter.eval(stonecutter.current.version, ">=1.17") -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

val awFileName = "rfu-${stonecutter.current.version}.accesswidener"

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

    maven(url = "https://repo.hypixel.net/repository/Hypixel/") {
        name = "Hypixel Api"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")

    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_language_kotlin")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-${property("resourceful_mc_version")}:${property("resourceful_version")}")!!)
    modImplementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-${property("resourcefulkt_mc_version")}:${property("resourcefulkt_version")}")!!)
    modImplementation(include("gg.essential:universalcraft-${property("universalcraft_mc_version")}-fabric:${property("universalcraft_version")}")!!)

    implementation(include("gg.essential:elementa:${property("elementa_version")}")!!)
    implementation("net.hypixel:mod-api:${property("hypixel_mod_api_version")}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${property("devauth_version")}")

    ksp(project(":processor"))
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/$awFileName")

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
        inputs.property("hypixel_mod_api", project.property("hypixel_mod_api_version"))
        inputs.property("aw_file", awFileName)

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.version,
            "minecraft" to project.property("mod.mc_dep"),
            "fabric_language_kotlin" to project.property("fabric_language_kotlin"),
            "hypixel_mod_api" to project.property("hypixel_mod_api_version"),
            "aw_file" to awFileName
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