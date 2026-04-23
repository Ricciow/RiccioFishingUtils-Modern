package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.party.PartyCommandManager
import cloud.glitchdev.rfu.utils.Chat
import net.minecraft.network.chat.Component

abstract class AbstractPartyCommand(
    override val name: String,
    override val aliases: List<String> = emptyList(),
    override val description: String = "",
    override val permission: PartyCommandPermission = PartyCommandPermission.ANY,
    override val responseTemplates: List<Pair<String, String>> = emptyList()
) : IPartyCommand {

    override fun init() {
        PartyCommandManager.register(this)
    }

    protected fun sendPartyMessage(message: String) {
        Chat.sendPartyMessage(message)
    }

    protected fun formatResponse(template: String, vararg args: Pair<String, Any>): String {
        var result = template
        args.forEach { (key, value) ->
            result = result.replace("{$key}", value.toString())
        }
        return result
    }

    override fun getRichResponse(sender: String, message: String, templateIndex: Int, match: MatchResult): Component {
        val component = Component.literal("${TextColor.LIGHT_BLUE}Party ${TextColor.DARK_GRAY}> ")
        
        val format = responseTemplates[templateIndex].second
        var formatted = format

        match.groupValues.forEachIndexed { index, value ->
            if (index > 0) {
                formatted = formatted.replace("{$index}", value)
            }
        }
        
        return component.append(Component.literal(formatted.replace("&", "§")))
    }
}
