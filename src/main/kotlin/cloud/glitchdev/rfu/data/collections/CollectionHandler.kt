package cloud.glitchdev.rfu.data.collections

import cloud.glitchdev.rfu.utils.JsonFile

object CollectionsHandler {
    private val jsonFile = JsonFile(
        filename = "collections.json",
        type = CollectionsData::class.java,
        defaultFactory = { CollectionsData() }
    )

    var totalInkSac: Long
        get() = jsonFile.data.totalInkSac
        set(value) {
            jsonFile.data.totalInkSac = value
            jsonFile.save()
        }
}