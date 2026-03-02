package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.manager.other.OtherManager
import cloud.glitchdev.rfu.manager.other.data.CakesEntry
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World

@RFUFeature
object CakeExpiredAlert : Feature {
    val CAKE_EATEN_REGEX = """(?:Big )?Yum! You (?:gain|refresh) (.+) for 48 hours!""".toRegex()
    private val lastOutdated : HashSet<CakesEntry.Cake> = hashSetOf()

    override fun onInitialize() {
        val cakes = OtherManager.getField("cakes") {
            CakesEntry()
        } as? CakesEntry ?: CakesEntry()

        registerTickEvent(interval = 300) {
            if(!OtherSettings.outdatedCake) return@registerTickEvent
            if(!World.isInSkyblock) return@registerTickEvent
            val outdated = cakes.getOutdatedCakes().toHashSet()

            val newOutDated = lastOutdated.minus(outdated)
            if(newOutDated.isNotEmpty()) {
                val message = TextUtils.rfuLiteral("${newOutDated.size} ${TextColor.GOLD}of your cakes just expired!", TextColor.YELLOW)
                Chat.sendMessage(message)
            }

            lastOutdated.clear()
            lastOutdated.addAll(outdated)
        }

        registerTickEvent(interval = 3000) {
            if(!OtherSettings.outdatedCake) return@registerTickEvent
            if(!World.isInSkyblock) return@registerTickEvent
            val outdated = cakes.getOutdatedCakes()

            if(outdated.isNotEmpty()) {
                val message = TextUtils.rfuLiteral("You have ${TextColor.YELLOW}${outdated.size} ${TextColor.GOLD}expired cakes!", TextColor.GOLD)
                Chat.sendMessage(message)
            }
        }

        registerGameEvent(CAKE_EATEN_REGEX) { _, _, match ->
            val effect = match?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            cakes.eatCake(effect)
        }
    }
}