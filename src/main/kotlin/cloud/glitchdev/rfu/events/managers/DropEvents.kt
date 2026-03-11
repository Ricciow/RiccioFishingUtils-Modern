package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.utils.dsl.escapeForRegex
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@AutoRegister
object DropEvents : RegisteredEvent {
    val RARE_DROP_REGEX = buildString {
        append("RARE DROP! (")
        append(RareDrops.entries.joinToString("|") { it.overrideRegex ?: it.toString().escapeForRegex() })
        append(""")(?: \(\+(\d+) ✯ Magic Find\))?""")
    }.toExactRegex()

    val DYE_REGEX = buildString {
        append("WOW! (.+) found a (")
        append(Dyes.entries.joinToString("|") { it.toString().escapeForRegex() })
        append(")!")
    }.toExactRegex()

    val DYE_ODD_REGEX = """.+ [\d,]+\/[\d,]+(?:\.\d+)?\w? \(\d+(?:\.\d+)?%\) chance!(?: \(\+(\d+)% ✯ Magic Find\))?""".toExactRegex()
    var currentDye : Dyes? = null

    override fun register() {
        ChatEvents.registerAllowGameEvent { text, overlay, _ ->
            if (overlay) return@registerAllowGameEvent true
            val string = text.string.removeFormatting()

            RARE_DROP_REGEX.find(string)?.groupValues?.let { (_, dropName, mfString) ->
                val rareDrop = RareDrops.getRelatedDrop(dropName) ?: return@let
                val magicFind = mfString.toIntOrNull()

                if (!RareDropEventManager.runTasks(rareDrop, magicFind)) return@registerAllowGameEvent false
            }

            DYE_REGEX.find(string)?.groupValues?.let { (_, username, dropName) ->
                if (!username.removeRankTag().isUser()) return@let
                currentDye = Dyes.getRelatedDye(dropName) ?: return@let
            }

            DYE_ODD_REGEX.find(string)?.groupValues?.let { (_, mfString) ->
                val magicFind = mfString.toIntOrNull()
                DyeDropEventManager.runTasks(currentDye ?: return@let, magicFind)
            }

            true
        }
    }

    fun registerRareDropEvent(
        priority: Int = 20,
        callback: (rareDrop: RareDrops, magicFind: Int?) -> Boolean
    ): RareDropEventManager.RareDropEvent {
        return RareDropEventManager.register(priority, callback)
    }

    fun registerDyeDropEvent(
        priority: Int = 20,
        callback: (dyeDrop: Dyes, magicFind : Int?) -> Unit
    ): DyeDropEventManager.DyeDropEvent {
        return DyeDropEventManager.register(priority, callback)
    }

    object RareDropEventManager : AbstractEventManager<(rareDrop: RareDrops, magicFind: Int?) -> Boolean, RareDropEventManager.RareDropEvent>() {
        override val runTasks: (RareDrops, Int?) -> Boolean = { rareDrop, magicFind ->
            var result = true
            safeExecution(mainThread = false) {
                tasks.forEach { event -> if (!event.callback(rareDrop, magicFind)) result = false }
            }
            result
        }

        fun register(
            priority: Int = 20,
            callback: (rareDrop: RareDrops, magicFind: Int?) -> Boolean
        ): RareDropEvent {
            return RareDropEvent(priority, callback).register()
        }

        class RareDropEvent(
            priority: Int = 20,
            callback: (rareDrop: RareDrops, magicFind: Int?) -> Boolean
        ) : ManagedTask<(rareDrop: RareDrops, magicFind: Int?) -> Boolean, RareDropEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object DyeDropEventManager : AbstractEventManager<(dyeDrop: Dyes, magicFind : Int?) -> Unit, DyeDropEventManager.DyeDropEvent>() {
        override val runTasks: (Dyes, Int?) -> Unit = { dyeDrop, magicFind ->
            safeExecution(mainThread = false) {
                tasks.forEach { event -> event.callback(dyeDrop, magicFind) }
            }
        }

        fun register(
            priority: Int = 20,
            callback: (dyeDrop: Dyes, magicFind : Int?) -> Unit
        ): DyeDropEvent {
            return DyeDropEvent(priority, callback).register()
        }

        class DyeDropEvent(
            priority: Int = 20,
            callback: (dyeDrop: Dyes, magicFind : Int?) -> Unit
        ) : ManagedTask<(dyeDrop: Dyes, magicFind : Int?) -> Unit, DyeDropEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}