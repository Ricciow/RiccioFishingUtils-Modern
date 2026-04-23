package cloud.glitchdev.rfu.feature.other.ignore

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object RemoveAllIgnoreSubCommand : SimpleCommand("removeall") {
    override val description: String = "Clear the entire ignore list"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val entry = IgnoreUtils.getIgnoredEntry()
        entry.clear()
        IgnoreUtils.saveIgnoredEntry(entry)
        context.source.sendFeedback(TextUtils.rfuLiteral("Cleared all users from the ignore list.", TextColor.LIGHT_GREEN))
        return 1
    }
}
