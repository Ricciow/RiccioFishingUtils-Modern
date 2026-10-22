package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.utils.Sounds

@RFUFeature
object RareAlert : Feature {
    var lastEntities : Set<SkyblockEntity> = setOf()

    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!SeaCreatureConfig.rareSc.enabled) return@registerMobDetectEvent
            val entities = entities.filter { !it.isRemoved() && (!it.hasHealth || it.health != "0") && SeaCreatures.get(it.sbName)?.rareSCAlert == true }.toSet()
            val newEntities = entities.minus(lastEntities)
            lastEntities = entities

            val seenEntities = mutableSetOf<String>()

            newEntities.filter { entity ->
                val result = !seenEntities.contains(entity.sbName)
                seenEntities.add(entity.sbName)
                result
            }.forEach { entity ->
                val sc = SeaCreatures.get(entity.sbName)
                SeaCreatureConfig.rareSc.show(*placeholders(sc, entity.sbName)) { !entity.isRemoved() }
            }

            if(newEntities.isNotEmpty() && SeaCreatureConfig.rareSc.sound) {
                Sounds.playSound("rfu:rare_sc", 1f, SeaCreatureConfig.rareSc.volume)
            }
        }

        registerLocationEvent {
            lastEntities = emptySet()
        }

        registerDisconnectEvent {
            lastEntities = emptySet()
        }
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
