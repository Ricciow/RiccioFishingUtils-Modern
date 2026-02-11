plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") apply false
}

stonecutter active "1.21.11"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "rfu"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
}
