plugins {
    id("fabric-loom") apply false
    id("net.fabricmc.fabric-loom") apply false
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("com.modrinth.minotaur") version "2.8.7"
}

stonecutter {
    properties.tags(current.version)
}

fun getProp(name: String): String {
    if (project.hasProperty(name)) return project.property(name).toString()

    val redirect = project.findProperty("stonecutter.redirect")?.toString()
    if (redirect != null) {
        val redirectedName = "$redirect.$name"
        if (project.hasProperty(redirectedName)) {
            return project.property(redirectedName).toString()
        }
    }
    return project.property(name).toString()
}

if (stonecutter.eval(stonecutter.current.version, ">=26.1")) {
    apply(plugin = "net.fabricmc.fabric-loom")
} else {
    apply(plugin = "fabric-loom")
}

version = "${getProp("mod.version")}+${stonecutter.current.version}"
base.archivesName = getProp("mod.id")

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
            required.version("fabric-api", getProp("modrinth.fapi_version"))
            required.version("fabric-language-kotlin", getProp("modrinth.kotlin_version"))
            required.version("hypixel-mod-api", getProp("modrinth.hypixel_version"))
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

val awVersion = if (stonecutter.eval(stonecutter.current.version, ">=26.1")) "26.1" else stonecutter.current.version
val awFileName = "rfu-$awVersion.accesswidener"

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

    modImpl("net.fabricmc:fabric-loader:${getProp("deps.fabric_loader")}")
    modImpl("net.fabricmc:fabric-language-kotlin:${getProp("fabric_language_kotlin")}")
    modImpl("net.fabricmc.fabric-api:fabric-api:${getProp("deps.fabric_api")}")

    val resConfig = "com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-${getProp("resourceful_mc_version")}:${getProp("resourceful_version")}"
    modImpl(resConfig)
    "include"(resConfig)

    val resConfigKt = if (stonecutter.eval(stonecutter.current.version, "< 26.1")) {
        "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-${getProp("resourcefulkt_mc_version")}:${getProp("resourcefulkt_version")}"
    } else {
        "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-${getProp("resourcefulkt_mc_version")}-rc-1:${getProp("resourcefulkt_version")}-beta.1"
    }
    modImpl(resConfigKt)
    "include"(resConfigKt)

    val univCraft = "gg.essential:universalcraft-${getProp("universalcraft_mc_version")}-fabric:${getProp("universalcraft_version")}"
    modImpl(univCraft)
    "include"(univCraft)

    val elementa = "gg.essential:elementa:${getProp("elementa_version")}"
    "implementation"(elementa)
    "include"(elementa)

    "implementation"("net.hypixel:mod-api:${getProp("hypixel_mod_api_version")}")

    modRuntime("me.djtheredstoner:DevAuth-fabric:${getProp("devauth_version")}")

    ksp(project(":processor"))
}

configure<net.fabricmc.loom.api.LoomGradleExtensionAPI> {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/$awFileName")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    val modsFolder = project.findProperty("stonecutter.redirect")?.toString() ?: stonecutter.current.version
    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs(
            "-Dmixin.debug.export=true",
            "-Dfabric.addMods=mods/$modsFolder",
            "-Ddevauth.enabled=true",
            "-Ddevauth.account=main"
        )
        runDir = "../../run"
    }
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion.toInt()))
    }
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
        inputs.property("id", getProp("mod.id"))
        inputs.property("name", getProp("mod.name"))
        inputs.property("version", project.version)
        inputs.property("minecraft", getProp("mod.mc_dep"))
        inputs.property("fabric_language_kotlin", getProp("fabric_language_kotlin"))
        inputs.property("hypixel_mod_api", getProp("hypixel_mod_api_version"))
        inputs.property("aw_file", awFileName)

        val props = mapOf(
            "id" to getProp("mod.id"),
            "name" to getProp("mod.name"),
            "version" to project.version,
            "minecraft" to getProp("mod.mc_dep"),
            "fabric_language_kotlin" to getProp("fabric_language_kotlin"),
            "hypixel_mod_api" to getProp("hypixel_mod_api_version"),
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