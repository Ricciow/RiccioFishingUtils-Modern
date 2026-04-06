plugins {
    id("fabric-loom") apply false
    id("net.fabricmc.fabric-loom") apply false
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("com.modrinth.minotaur") version "2.8.7"
}

if (stonecutter.eval(stonecutter.current.version, ">=26.1")) {
    apply(plugin = "net.fabricmc.fabric-loom")
} else {
    apply(plugin = "fabric-loom")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

if (project.name != rootProject.name && project.name != "processor") {
    modrinth {
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set(System.getenv("MODRINTH_PROJECT_ID"))
        versionNumber.set(project.version.toString())
        versionType.set("release")

        // Conditionally set the upload task based on the version
        val uploadTaskName = if (stonecutter.eval(stonecutter.current.version, ">=26.1")) "jar" else "remapJar"
        uploadFile.set(tasks.named(uploadTaskName))

        gameVersions.add(stonecutter.current.version)
        loaders.add("fabric")

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
    stonecutter.eval(stonecutter.current.version, ">=26.1") -> JavaVersion.VERSION_25
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
    "minecraft"("com.mojang:minecraft:${stonecutter.current.version}")

    if (stonecutter.eval(stonecutter.current.version, "<26.1")) {
        val loomExt = project.extensions.getByType<net.fabricmc.loom.api.LoomGradleExtensionAPI>()
        "mappings"(loomExt.officialMojangMappings())
    }

    // Modern dependency naming wrappers
    val isModern = stonecutter.eval(stonecutter.current.version, ">=26.1")
    val modImpl = if (isModern) "implementation" else "modImplementation"
    val modRuntime = if (isModern) "runtimeOnly" else "modRuntimeOnly"

    modImpl("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImpl("net.fabricmc:fabric-language-kotlin:${property("fabric_language_kotlin")}")
    modImpl("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    val resConfig = "com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-${property("resourceful_mc_version")}:${property("resourceful_version")}"
    modImpl(resConfig)
    "include"(resConfig)

    val resConfigKt = "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-${property("resourcefulkt_mc_version")}:${property("resourcefulkt_version")}"
    modImpl(resConfigKt)
    "include"(resConfigKt)

    val univCraft = "gg.essential:universalcraft-${property("universalcraft_mc_version")}-fabric:${property("universalcraft_version")}"
    modImpl(univCraft)
    "include"(univCraft)

    val elementa = "gg.essential:elementa:${property("elementa_version")}"
    "implementation"(elementa)
    "include"(elementa)

    "implementation"("net.hypixel:mod-api:${property("hypixel_mod_api_version")}")

    modRuntime("me.djtheredstoner:DevAuth-fabric:${property("devauth_version")}")

    ksp(project(":processor"))
}

configure<net.fabricmc.loom.api.LoomGradleExtensionAPI> {
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

    if (stonecutter.eval(stonecutter.current.version, "<26.1")) {
        named("remapJar", net.fabricmc.loom.task.RemapJarTask::class) {
            destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
        }
    } else {
        named("jar", Jar::class) {
            destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
        }
    }
}