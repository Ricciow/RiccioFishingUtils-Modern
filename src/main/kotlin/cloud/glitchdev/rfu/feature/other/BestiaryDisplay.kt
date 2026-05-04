package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.BestiaryHUD
import cloud.glitchdev.rfu.utils.Tablist
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@RFUFeature
object BestiaryDisplay : Feature {
    private val bestiaryRegex = """([\w\s]+) (\d+): ([\d,]+)/([\d,]+)""".toExactRegex()

    var bestiaries = listOf<BestiaryEntry>()
        private set

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            bestiaries = Tablist.getTablistAsStrings().mapNotNull { text ->
                val result = bestiaryRegex.find(text.removeFormatting()) ?: return@mapNotNull null
                
                val name = result.groupValues[1].trim()
                val currentTier = result.groupValues[2].toInt()
                val current = result.groupValues[3].replace(",", "").toInt()
                val goal = result.groupValues[4].replace(",", "").toInt()
                
                BestiaryEntry(name, currentTier, current, goal)
            }

            BestiaryHUD.updateState()
        }
    }

    data class BestiaryEntry(val name: String, val currentTier : Int, val current: Int, val goal: Int)
}
