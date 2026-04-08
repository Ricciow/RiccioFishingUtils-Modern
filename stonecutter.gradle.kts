plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") apply false
}

stonecutter active "26.1"

fun getProp(p: Project, name: String): String {
    if (p.hasProperty(name)) return p.property(name).toString()

    val redirect = p.findProperty("stonecutter.redirect")?.toString()
    if (redirect != null) {
        val redirectedName = "$redirect.$name"
        if (p.hasProperty(redirectedName)) return p.property(redirectedName).toString()
        if (p.rootProject.hasProperty(redirectedName)) return p.rootProject.property(redirectedName).toString()
    }
    return p.property(name).toString()
}

stonecutter parameters {
    swaps["mod_version"] = "\"" + getProp(project, "mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = getProp(project, "mod.id") != "rfu"
    dependencies["fapi"] = getProp(node.project, "deps.fabric_api")
}
