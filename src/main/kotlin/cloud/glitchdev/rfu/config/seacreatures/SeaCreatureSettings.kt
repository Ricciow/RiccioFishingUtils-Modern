package cloud.glitchdev.rfu.config.seacreatures

data class SeaCreatureSettings(
    val creatures: Map<String, SeaCreatureSetting>
)

{
    companion object {
        fun empty(): SeaCreatureSettings = SeaCreatureSettings(mapOf())
    }

}

data class SeaCreatureSetting(
    val name: String?, val plural: String?, val article: String?, val special: Boolean?, val format: String?
)
