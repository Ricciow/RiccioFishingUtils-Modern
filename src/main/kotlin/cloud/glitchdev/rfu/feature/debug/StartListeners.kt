package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.EventScopes
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object StartListeners : SimpleCommand("startlisteners") {
    override val description: String = "Starts RFU's stopped event listeners"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val started = try {
            EventScopes.start()
        } catch (failure: Exception) {
            RFULogger.error("Failed to start RFU listeners", failure)
            context.source.sendError(TextUtils.rfuLiteral("Some RFU listeners failed to start. Check the log for details."))
            return 0
        }
        if (!started) {
            context.source.sendFeedback(TextUtils.rfuLiteral("${TextColor.YELLOW}RFU listeners are already running."))
            return 1
        }
        context.source.sendFeedback(TextUtils.rfuLiteral("${TextColor.LIGHT_GREEN}Started RFU listeners."))
        return 1
    }
}
