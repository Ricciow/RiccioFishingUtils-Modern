package cloud.glitchdev.rfu.config.seacreatures

data class SeaCreatureSettings(
    val creatures: Map<String, SeaCreatureSetting>
) {
    companion object {
        fun empty(): SeaCreatureSettings = SeaCreatureSettings(mapOf())
    }
}

data class SeaCreatureSetting(
    val name: String?,
    val plural: String?,
    val article: String?,
    val special: Boolean?,
    val style: String?,
    val catchMessage: String?,
    val liquidType: String?,
    val category: String?,
    val lsRangeEnabled: Boolean?,
    val conditions: SeaCreatureConditions?,
    val bossbar: Boolean?,
    val gdragAlert: Boolean?,
    val rareSCAlert: Boolean?,
    val scDisplayColor: String?
) {
    companion object {
        fun empty(): SeaCreatureSetting = SeaCreatureSetting(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null
        )
    }
}

data class SeaCreatureConditions(
    val isFestival: Boolean? = null,
    val isSpooky: Boolean? = null,
    val hotspot: String? = null,
    val bait: String? = null,
    val coords: List<SeaCreatureCoordRange>? = null
)

data class SeaCreatureCoordRange(
    val x0: Double, val x1: Double,
    val y0: Double, val y1: Double,
    val z0: Double, val z1: Double,
    val exclude: Boolean? = false
)
