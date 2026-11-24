package cloud.glitchdev.rfu.events.managers.loot

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.data.drops.DeadMobTracker
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.dsl.escapeForRegex
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import cloud.glitchdev.rfu.events.managers.chat.ChatEvents

@AutoRegister
object DropEvents : RegisteredEvent {
    val RARE_DROP_REGEX = buildString {
        append("RARE DROP! (")
        append(RareDrops.entries.joinToString("|") { it.overrideRegex ?: it.toString().escapeForRegex() })
        append(""")(?: \(\+(\d+) ? Magic Find\))?""")
    }.toExactRegex()

    val DYE_REGEX = buildString {
        append("WOW! (.+) found (?:an? )?(")
        append(Dyes.entries.joinToString("|") { it.toString().escapeForRegex() })
        append(")!")
    }.toExactRegex()

    val DYE_ODD_REGEX = """.+ [\d,]+\/[\d,]+(?:\.\d+)?\w? \(\d+(?:\.\d+)?%\) chance!(?: \(\+(\d+)%  Magic Find\))?""".toExactRegex()
    var currentDye : Dyes? = null
    var dyeTimestamp : Long = 0L

    override fun register() {
        ChatEvents.registerGameEvent { text, overlay, _ ->
            if (overlay) return@registerGameEvent
            val string = text.string.removeFormatting()

            RARE_DROP_REGEX.find(string)?.groupValues?.let { (_, dropName, mfString) ->
                val rareDrop = RareDrops.getRelatedDrop(dropName) ?: return@let
                val magicFind = mfString.toIntOrNull()
                val dropTimestamp = System.currentTimeMillis()
                val playerPos = mc.player?.position()

                if (rareDrop.relatedScs.size == 1) {
                    val mobName = rareDrop.relatedScs.first().scName
                    DeadMobTracker.tryConsumeByName(mobName, dropTimestamp, windowMs = 50L)
                    RareDropEventManager.runTasks(rareDrop, magicFind, mobName)
                } else {
                    DeadMobTracker.findOrWait(rareDrop.relatedScs, playerPos, dropTimestamp, windowMs = 50L) { mobName ->
                        RareDropEventManager.runTasks(rareDrop, magicFind, mobName)
                    }
                }
            }

            DYE_REGEX.find(string)?.groupValues?.let { (_, username, dropName) ->
                if (!username.removeRankTag().isUser()) return@let
                currentDye = Dyes.getRelatedDye(dropName) ?: return@let
                dyeTimestamp = System.currentTimeMillis()
            }

            DYE_ODD_REGEX.find(string)?.groupValues?.let { (_, mfString) ->
                val dye = currentDye ?: return@let
                currentDye = null
                val magicFind = mfString.toIntOrNull()
                val dropTimestamp = dyeTimestamp
                val playerPos = mc.player?.position()

                if (dye.relatedScs.size == 1) {
                    val mobName = dye.relatedScs.first().scName
                    DeadMobTracker.tryConsumeByName(mobName, dropTimestamp, windowMs = 50L)
                    DyeDropEventManager.runTasks(dye, magicFind, mobName)
                } else {
                    DeadMobTracker.findOrWait(dye.relatedScs, playerPos, dropTimestamp, windowMs = 50L) { mobName ->
                        DyeDropEventManager.runTasks(dye, magicFind, mobName)
                    }
                }
            }
        }
    }

    fun registerRareDropEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (rareDrop: RareDrops, magicFind: Int?) -> Boolean
    ): RareDropEventManager.RareDropEvent {
        return registerRareDropEvent(priority) { rareDrop, magicFind, _ ->
            callback(rareDrop, magicFind)
        }
    }

    fun registerRareDropEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (rareDrop: RareDrops, magicFind: Int?, mobName: String?) -> Boolean
    ): RareDropEventManager.RareDropEvent {
        return RareDropEventManager.register(priority, callback)
    }

    fun registerDyeDropEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (dyeDrop: Dyes, magicFind : Int?) -> Unit
    ): DyeDropEventManager.DyeDropEvent {
        return registerDyeDropEvent(priority) { dyeDrop, magicFind, _ ->
            callback(dyeDrop, magicFind)
        }
    }

    fun registerDyeDropEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (dyeDrop: Dyes, magicFind: Int?, mobName: String?) -> Unit
    ): DyeDropEventManager.DyeDropEvent {
        return DyeDropEventManager.register(priority, callback)
    }
}
