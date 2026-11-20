package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.utils.Sounds

@RFUFeature
object RareAlert : Feature {
    private const val FIERY_SCUTTLER = "Fiery Scuttler"
    private const val SCUTTLERS_PER_ALERT = 6
    private var scuttlersSeen = 0
    var lastEntities : Set<SkyblockEntity> = setOf()

    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!SeaCreatureConfig.rareSc.enabled) return@registerMobDetectEvent
            val entities = entities.filter { !it.isRemoved() && (!it.hasHealth || it.healthStr != "0") && SeaCreatures.get(it.sbName)?.rareSCAlert == true }.toSet()
            val alerts = (entities - lastEntities).filter(::shouldAlert)
            lastEntities = entities

            alerts.distinctBy { it.sbName }.forEach { entity ->
                val sc = SeaCreatures.get(entity.sbName)
                SeaCreatureConfig.rareSc.show(*placeholders(sc, entity.sbName)) { !entity.isRemoved() }
            }

            if (alerts.isNotEmpty() && SeaCreatureConfig.rareSc.sound) {
                Sounds.playSound("rfu:rare_sc", 1f, SeaCreatureConfig.rareSc.volume)
            }
        }

        registerMobDisposeEvent { removed ->
            if (lastEntities.isEmpty()) return@registerMobDisposeEvent
            lastEntities = lastEntities - removed
            if (lastEntities.none(::isFieryScuttler)) {
                scuttlersSeen = 0
            }
        }
        registerLocationEvent { reset() }
        registerDisconnectEvent { reset() }
    }

    private fun isFieryScuttler(entity: SkyblockEntity): Boolean = entity.sbName == FIERY_SCUTTLER

    private fun shouldAlert(entity: SkyblockEntity): Boolean {
        if (!isFieryScuttler(entity)) return true

        val alert = scuttlersSeen == 0
        scuttlersSeen = (scuttlersSeen + 1) % SCUTTLERS_PER_ALERT
        return alert
    }

    private fun reset() {
        lastEntities = emptySet()
        scuttlersSeen = 0
    }

    private fun placeholders(sc: SeaCreatures?, fallbackName: String = ""): Array<Pair<String, Any>> {
        val name = sc?.scDisplayName ?: fallbackName
        val color = sc?.scDisplayColor?.ifEmpty { "§f" } ?: "§f"
        val style = sc?.style ?: ""
        val article = sc?.article ?: ""
        val articleUpper = article.replaceFirstChar { it.uppercaseChar() }
        val plural = sc?.plural ?: name
        val mob = sc?.getSingularNameWithArticle() ?: name

        return arrayOf(
            "article" to article, "article_upper" to articleUpper,
            "name" to name, "style" to style, "color" to color,
            "plural" to plural, "mob" to mob, "mobs" to plural
        )
    }
}
