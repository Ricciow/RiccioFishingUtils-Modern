package cloud.glitchdev.rfu.manager.catches

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.WorldChangeEvents.registerWorldChangeEvent
import cloud.glitchdev.rfu.utils.Command
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.utils.TextUtils
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal

@AutoRegister
object CatchTracker : RegisteredEvent {
    val catchesFile = JsonFile(
        filename = "catches.json",
        type = CatchHistory::class.java,
        defaultFactory = { CatchHistory() }
    )

    val catchHistory = catchesFile.data

    override fun register() {
        registerSeaCreatureCatchEvent(0) { sc ->
            catchHistory.registerCatch(sc)
        }

        registerWorldChangeEvent { _, _, _ ->
            catchesFile.save()
        }

        registerShutdownEvent {
            catchesFile.save()
        }

        Command.registerCommand(
            literal("rfucleanexcesshistory")
                .then(argument("max history", IntegerArgumentType.integer())
                    .executes { context ->
                        val maxSize = IntegerArgumentType.getInteger(context, "max history")
                        catchHistory.cleanExcessData(maxSize)
                        catchesFile.save()
                        context.source.sendFeedback(TextUtils.rfuLiteral("Cleaned catch history data that exceeds $maxSize entries.",
                            TextStyle(TextColor.LIGHT_GREEN)))
                        1
                    }
                )
        )
    }
}