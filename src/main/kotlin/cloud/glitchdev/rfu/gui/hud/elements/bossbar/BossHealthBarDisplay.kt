package cloud.glitchdev.rfu.gui.hud.elements.bossbar

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.utils.dsl.parseHealthValue
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import kotlin.math.max

@HudElement
object BossHealthBarDisplay : AbstractHudElement("bossHealthBar") {
    val entities: MutableSet<SkyblockEntity> = mutableSetOf()
    val bars : MutableList<BossHealthBar> = mutableListOf()

    override val enabled: Boolean
        get() = (super.enabled || entities.isNotEmpty()) && SeaCreatureConfig.bossHealthBars

    val barsContainer = UIContainer().constrain {
        width = (400 * scale).pixels()
        height = (9 * scale).pixels()
    } childOf this

    private fun formatHealthValue(value: Int): String {
        return when {
            value >= 1_000_000 -> {
                val millions = value / 1_000_000.0
                val formatted = String.format("%.1f", millions).replace(",", ".")
                if (formatted.endsWith(".0")) formatted.dropLast(2) + "M" else formatted + "M"
            }
            value >= 1_000 -> {
                val thousands = value / 1_000.0
                val formatted = String.format("%.1f", thousands).replace(",", ".")
                if (formatted.endsWith(".0")) formatted.dropLast(2) + "k" else formatted + "k"
            }
            else -> value.toString()
        }
    }

    override fun onUpdateState() {
        if (isEditing) {
            getOrAddBar(0).forceRendering = true
        }

        // Group / Merge entities
        val mergedInfos = mutableListOf<BossBarEntityInfo>()
        
        // We group entities that should be merged
        val (toMerge, toNotMerge) = entities.partition { entity ->
            val sc = SeaCreatures.get(entity.sbName)
            sc != null && sc.mergeBossbarHp
        }

        // Group toMerge by sbName
        val grouped = toMerge.groupBy { it.sbName }
        grouped.forEach { (sbName, groupEntities) ->
            val count = groupEntities.size
            val totalHealthVal = groupEntities.sumOf { it.health.parseHealthValue() }
            val totalMaxHealthVal = groupEntities.sumOf { it.maxHealth.parseHealthValue() }
            val totalHealth = formatHealthValue(totalHealthVal)
            val totalMaxHealth = formatHealthValue(totalMaxHealthVal)
            val shurikenCount = groupEntities.count { it.isShurikened }
            val outdated = groupEntities.all { it.outdatedNametag() }
            
            mergedInfos.add(
                BossBarEntityInfo(
                    sbName = sbName,
                    health = totalHealth,
                    maxHealth = totalMaxHealth,
                    shurikenCount = shurikenCount,
                    outdatedNametag = outdated,
                    count = count,
                    showCount = true
                )
            )
        }

        // Add non-merged entities
        toNotMerge.forEach { entity ->
            mergedInfos.add(
                BossBarEntityInfo(
                    sbName = entity.sbName,
                    health = entity.health,
                    maxHealth = entity.maxHealth,
                    shurikenCount = if (entity.isShurikened) 1 else 0,
                    outdatedNametag = entity.outdatedNametag(),
                    count = 1,
                    showCount = false
                )
            )
        }

        mergedInfos.forEachIndexed { index, info ->
            val bar = getOrAddBar(index)
            bar.updateInfo(info)
        }

        bars.forEachIndexed { index, bar ->
            bar.updateScale(scale)
            bar.constrain {
                y = SiblingConstraint(2 * scale)
                height = (9 * scale).pixels()
            }

            if(index < mergedInfos.size) return@forEachIndexed
            bar.updateInfo(null)
        }

        barsContainer.constrain {
            width = (400 * scale).pixels()
            if (!isEditing) {
                height = ((2 * (max(mergedInfos.size-1, 0)) + 9 * (mergedInfos.size)) * scale).pixels()
            } else {
                height = ((2 * (max(mergedInfos.size-1, 0)) + 9 * (max(mergedInfos.size, 1))) * scale).pixels()
            }
        }
    }

    private fun getOrAddBar(index : Int) : BossHealthBar {
        val bar = bars.getOrElse(index) {
            val bossBar = BossHealthBar(null)
            bars.add(bossBar)
            bossBar.constrain {
                x = CenterConstraint()
                y = SiblingConstraint(2 * scale)
                width = 100.percent()
                height = (9 * scale).pixels()
            } childOf barsContainer
        }

        return bar
    }

    fun updateEntities(entities : Set<SkyblockEntity>) {
        this.entities.clear()
        this.entities.addAll(entities)
        updateState()
    }
}