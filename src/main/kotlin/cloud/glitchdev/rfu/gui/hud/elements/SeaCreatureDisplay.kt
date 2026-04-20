package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor.WHITE
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.fishing.BaitManager
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import net.minecraft.world.phys.Vec3

@HudElement
object SeaCreatureDisplay : AbstractHudElement("seaCreatureDisplay") {
    private var rows: List<UIContainer> = emptyList()

    private val isFishing: Boolean
        get() = FishingSession.isFishing

    override val enabled: Boolean
        get() = SeaCreatureConfig.seaCreatureDisplay &&
                (super.enabled || !SeaCreatureConfig.seaCreatureOnlyWhenFishing || isFishing)

    init {
        constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        registerSeaCreatureCatchEvent { _, _, _, _, _ ->
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        val currentIsland = World.island ?: return

        val catchHistory = CatchTracker.catchHistory
        var lastHotspot = catchHistory.lastHotspot
        var lastPos = catchHistory.lastPos
        var lastBait = catchHistory.lastBait
        var lastLiquid = catchHistory.lastLiquid

        val player = mc.player
        if (lastPos == Vec3.ZERO && player != null) {
            lastPos = player.position()
            lastHotspot = HotSpotEvents.getHotspotAt(lastPos)
            lastBait = BaitManager.lastBait
            lastLiquid = lastHotspot?.liquid
        }

        val selectedScs = SeaCreatures.entries.filter {
            if (!it.category.islands.contains(currentIsland)) return@filter false
            if (lastPos != Vec3.ZERO && !it.condition(lastHotspot, lastPos, lastBait)) return@filter false
            if (lastLiquid != null && it.liquidType != lastLiquid) return@filter false
            true
        }.sortedByDescending { it.weight }

        if (selectedScs.isEmpty()) {
            clearRows()
            return
        }

        val entries = selectedScs.map { sc ->
            val record = catchHistory.getOrAdd(sc)
            val color = sc.scDisplayColor.ifEmpty { WHITE }
            "$color${sc.getNameWithoutArticle()}" to record.total.toString()
        }

        setNameValueText(entries)
    }

    private fun clearRows() {
        rows.forEach { removeChild(it) }
        rows = emptyList()
    }

    fun setNameValueText(entries: List<Pair<String, String>>) {
        clearRows()

        val font = mc.font
        val scale = this.scale
        val minSpacingPx = 5

        val maxWidth = entries.maxOf { (name, value) ->
            font.width(name) + font.width(value) + minSpacingPx
        }

        rows = entries.mapIndexed { index, (name, value) ->
            UIContainer().constrain {
                width = maxWidth.pixels()
                height = ChildBasedMaxSizeConstraint()
                x = 0.pixels()
                y = if (index == 0) 0.pixels() else SiblingConstraint(padding = 2f)
            }.apply {
                UIText(name, shadow = true).constrain {
                    x = 0.pixels()
                    y = 0.pixels()
                    width = ScaledTextConstraint(scale)
                    height = TextAspectConstraint()
                } childOf this

                UIText(value, shadow = true).constrain {
                    x = 0.pixels(alignOpposite = true)
                    y = 0.pixels()
                    width = ScaledTextConstraint(scale)
                    height = TextAspectConstraint()
                } childOf this
            } childOf this
        }
    }
}