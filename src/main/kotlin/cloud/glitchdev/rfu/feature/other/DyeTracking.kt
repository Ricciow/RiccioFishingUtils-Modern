package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.network.DyeHttp
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

@RFUFeature
object DyeTracking : Feature {
    override fun onInitialize() {

    }

    @Command
    object DyeCommand : SimpleCommand("rfudyes") {
        override val description: String = "Sends the current dyes in rotation in chat:"

        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            val text = Component.literal("")

            val currentDyes = DyeHttp.currentDyes

            if(currentDyes != null && !currentDyes.isEmpty()) {
                text.append(TextUtils.rfuLiteral("Dyes (Year ${currentDyes.sbYear}): ", TextStyle(YELLOW)))
                text.append(Component.literal("\n$YELLOW - $WHITE${currentDyes.get3xDye()} $YELLOW(3x)"))
                currentDyes.get2xDyes().forEach { dye ->
                    text.append(Component.literal("\n$YELLOW - $WHITE${dye} $YELLOW(2x)"))
                }
            } else {
                if(BackendSettings.backendAccepted && BackendSettings.shareDyeData) {
                    text.append(TextUtils.rfuLiteral("No one has checked the dyes yet! ${GOLD}Go be the first!", TextStyle(LIGHT_RED)))
                } else {
                    text.append(TextUtils.rfuLiteral("Current dyes are not available :(", TextStyle(LIGHT_RED)))
                }
            }

            context.source.sendFeedback(text)

            return 1
        }
    }
}