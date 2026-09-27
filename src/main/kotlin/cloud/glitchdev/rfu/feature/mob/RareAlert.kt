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
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.toMcCodes

@RFUFeature
object RareAlert : Feature {
    var lastEntities : Set<SkyblockEntity> = setOf()

    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!SeaCreatureConfig.detectionAlert) return@registerMobDetectEvent
            val entities = entities.filter { !it.isRemoved() && it.health != "0" && SeaCreatures.get(it.sbName)?.rareSCAlert == true }.toSet()
            val newEntities = entities.minus(lastEntities)
            lastEntities = entities

            val seenEntities = mutableSetOf<String>()

            newEntities.filter { entity ->
                val result = !seenEntities.contains(entity.sbName)
                seenEntities.add(entity.sbName)
                result
            }.forEach { entity ->
                val sc = SeaCreatures.get(entity.sbName)
                val title = formatAlert(sc, entity.sbName)
                Title.showTitle(title, extraTicks = SeaCreatureConfig.rareScTitleExtraTicks) { !entity.isRemoved() }
            }

            if(newEntities.isNotEmpty() && SeaCreatureConfig.rareScSound) {
                Sounds.playSound("rfu:rare_sc", 1f, SeaCreatureConfig.rareScSoundVolume)
            }
        }

        registerLocationEvent {
            lastEntities = emptySet()
        }

        registerDisconnectEvent {
            lastEntities = emptySet()
        }
    }

    fun formatAlert(sc: SeaCreatures?, fallbackName: String = ""): String {
        val name = sc?.scDisplayName ?: fallbackName
        val color = sc?.scDisplayColor?.ifEmpty { "§f" } ?: "§f"
        val style = sc?.style ?: ""
        val article = sc?.article ?: ""
        val articleUpper = article.replaceFirstChar { it.uppercaseChar() }
        val plural = sc?.plural ?: name
        val mob = sc?.getSingularNameWithArticle() ?: name

        return SeaCreatureConfig.rareScAlertPreset
            .replace("{article}", article)
            .replace("{article_upper}", articleUpper)
            .replace("{name}", name)
            .replace("{style}", style)
            .replace("{color}", color)
            .replace("{plural}", plural)
            .replace("{mob}", mob)
            .replace("{mobs}", plural)
            .toMcCodes()
    }

    fun preview() {
        val sc = SeaCreatures.entries.filter { it.special }.randomOrNull() ?: SeaCreatures.entries.randomOrNull()
        if (sc != null) {
            val title = formatAlert(sc)
            Title.showTitle(title, extraTicks = SeaCreatureConfig.rareScTitleExtraTicks)
        }
    }
}
