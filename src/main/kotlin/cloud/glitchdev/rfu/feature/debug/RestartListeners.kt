package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.events.EventScopes
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object RestartListeners : SimpleCommand("restartlisteners") {
    override val description: String = "Restarts RFU's active event listeners"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        try {
            EventScopes.restart()
        } catch (failure: Exception) {
            RFULogger.error("Failed to restart RFU listeners", failure)
            context.source.sendError(TextUtils.rfuLiteral("Some RFU listeners failed to restart. Check the log for details."))
            return 0
        }
        context.source.sendFeedback(TextUtils.rfuLiteral("Restarted RFU listeners."))
        return 1
    }
}
