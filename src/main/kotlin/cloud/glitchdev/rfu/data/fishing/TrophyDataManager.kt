package cloud.glitchdev.rfu.data.fishing

import cloud.glitchdev.rfu.utils.JsonFile

object TrophyDataManager {
    val file = JsonFile(
        filename = "trophy_data.json",
        type = TrophyData::class.java,
        defaultFactory = { TrophyData() }
    )

    val data: TrophyData
        get() = file.data

    fun save() {
        file.save()
    }
}
