package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.chat.registerGameEvent
import cloud.glitchdev.rfu.constants.fishing.Trophy
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.constants.fishing.TrophyType
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.registration.ScopedRegistration
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@AutoRegister
object TrophyCatchEvents : RegisteredEvent {
    val TROPHY_FROG_REGEX = """(?:\s*)?TROPHY FROG! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)(?:\s+x(\d+))?!""".toExactRegex()
    val TROPHY_FISH_REGEX = """(?:\s*)?TROPHY FISH! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)(?:\s+x(\d+))?!""".toExactRegex()

    override fun register(scope: EventScope) {
        scope.registerGameEvent(TROPHY_FROG_REGEX) { _, _, matches ->
            val name = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return@registerGameEvent
            val tier = TrophyTier.entries.find { it.name == tierStr } ?: return@registerGameEvent
            val amount = matches.groupValues.getOrNull(3)?.toIntOrNull() ?: 1
            val frog = TrophyFrog.fromName(name) ?: return@registerGameEvent
            TrophyFrogCatchEventManager.runTasks(frog, tier, amount)
        }

        scope.registerGameEvent(TROPHY_FISH_REGEX) { _, _, matches ->
            val name = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return@registerGameEvent
            val tier = TrophyTier.entries.find { it.name == tierStr } ?: return@registerGameEvent
            val amount = matches.groupValues.getOrNull(3)?.toIntOrNull() ?: 1
            val fish = TrophyFish.fromName(name) ?: return@registerGameEvent
            TrophyFishCatchEventManager.runTasks(fish, tier, amount)
        }
    }

    @ScopedRegistration
    internal fun registerTrophyCatchEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (trophy: Trophy, tier: TrophyTier, type: TrophyType, amount: Int) -> Unit
    ): Pair<TrophyFrogCatchEventManager.TrophyFrogCatchEvent, TrophyFishCatchEventManager.TrophyFishCatchEvent> {
        val frogEvent = TrophyFrogCatchEventManager.TrophyFrogCatchEvent(priority) { frog, tier, amount ->
            callback(frog, tier, TrophyType.FROG, amount)
        }.register()
        val fishEvent = TrophyFishCatchEventManager.TrophyFishCatchEvent(priority) { fish, tier, amount ->
            callback(fish, tier, TrophyType.FISH, amount)
        }.register()
        return Pair(frogEvent, fishEvent)
    }
}
