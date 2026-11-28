package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.EventScopes
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object StopListeners : SimpleCommand("stoplisteners") {
    override val description: String = "Stops RFU's active event listeners"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val stopped = try {
            EventScopes.stop()
        } catch (failure: Exception) {
            RFULogger.error("Failed to stop RFU listeners", failure)
            context.source.sendError(TextUtils.rfuLiteral("Some RFU listeners failed to stop. Check the log for details."))
            return 0
        }
        if (!stopped) {
            context.source.sendFeedback(TextUtils.rfuLiteral("${TextColor.YELLOW}RFU listeners are already stopped."))
            return 1
        }
        context.source.sendFeedback(TextUtils.rfuLiteral("${TextColor.LIGHT_GREEN}Stopped RFU listeners."))
        return 1
    }
}
