package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.constants.fishing.TrophyType

@AutoRegister
object TrophyCatchEvents : RegisteredEvent {
    private val TROPHY_FROG_REGEX = """ TROPHY FROG! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)!""".toRegex(RegexOption.IGNORE_CASE)
    private val TROPHY_FISH_REGEX = """ TROPHY FISH! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)!""".toRegex(RegexOption.IGNORE_CASE)

    override fun register() {
        registerGameEvent(TROPHY_FROG_REGEX) { _, _, matches ->
            val name = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return@registerGameEvent
            val tier = TrophyTier.entries.find { it.name == tierStr } ?: return@registerGameEvent
            TrophyFrogCatchEventManager.runTasks(name, tier)
        }

        registerGameEvent(TROPHY_FISH_REGEX) { _, _, matches ->
            val name = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return@registerGameEvent
            val tier = TrophyTier.entries.find { it.name == tierStr } ?: return@registerGameEvent
            TrophyFishCatchEventManager.runTasks(name, tier)
        }
    }

    fun registerTrophyFrogCatchEvent(
        priority: Int = 20,
        callback: (name: String, tier: TrophyTier) -> Unit
    ): TrophyFrogCatchEventManager.TrophyFrogCatchEvent {
        return TrophyFrogCatchEventManager.register(priority, callback)
    }

    fun registerTrophyFishCatchEvent(
        priority: Int = 20,
        callback: (name: String, tier: TrophyTier) -> Unit
    ): TrophyFishCatchEventManager.TrophyFishCatchEvent {
        return TrophyFishCatchEventManager.register(priority, callback)
    }

    fun registerTrophyCatchEvent(
        priority: Int = 20,
        callback: (name: String, tier: TrophyTier, type: TrophyType) -> Unit
    ): Pair<TrophyFrogCatchEventManager.TrophyFrogCatchEvent, TrophyFishCatchEventManager.TrophyFishCatchEvent> {
        val frogEvent = registerTrophyFrogCatchEvent(priority) { name, tier ->
            callback(name, tier, TrophyType.FROG)
        }
        val fishEvent = registerTrophyFishCatchEvent(priority) { name, tier ->
            callback(name, tier, TrophyType.FISH)
        }
        return Pair(frogEvent, fishEvent)
    }

    object TrophyFrogCatchEventManager : AbstractEventManager<(name: String, tier: TrophyTier) -> Unit, TrophyFrogCatchEventManager.TrophyFrogCatchEvent>() {
        override val runTasks: (name: String, tier: TrophyTier) -> Unit = { name, tier ->
            safeExecution {
                tasks.forEach { task -> task.callback(name, tier) }
            }
        }

        fun register(priority: Int = 20, callback: (name: String, tier: TrophyTier) -> Unit): TrophyFrogCatchEvent {
            return TrophyFrogCatchEvent(priority, callback).register()
        }

        class TrophyFrogCatchEvent(
            priority: Int = 20,
            callback: (name: String, tier: TrophyTier) -> Unit
        ) : ManagedTask<(name: String, tier: TrophyTier) -> Unit, TrophyFrogCatchEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object TrophyFishCatchEventManager : AbstractEventManager<(name: String, tier: TrophyTier) -> Unit, TrophyFishCatchEventManager.TrophyFishCatchEvent>() {
        override val runTasks: (name: String, tier: TrophyTier) -> Unit = { name, tier ->
            safeExecution {
                tasks.forEach { task -> task.callback(name, tier) }
            }
        }

        fun register(priority: Int = 20, callback: (name: String, tier: TrophyTier) -> Unit): TrophyFishCatchEvent {
            return TrophyFishCatchEvent(priority, callback).register()
        }

        class TrophyFishCatchEvent(
            priority: Int = 20,
            callback: (name: String, tier: TrophyTier) -> Unit
        ) : ManagedTask<(name: String, tier: TrophyTier) -> Unit, TrophyFishCatchEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
