package cloud.glitchdev.rfu.party

import net.minecraft.network.chat.Component

enum class PartyCommandPermission {
    LEADER_ONLY,
    MEMBER_ONLY,
    SELF_TRIGGER
}

interface IPartyCommand {
    val name: String
    val aliases: List<String>
    val description: String
    val permission: List<PartyCommandPermission>
    val responseTemplates: List<Pair<String, String>>

    fun isEnabled(): Boolean
    fun execute(sender: String, args: List<String>)
    fun getRichResponse(sender: String, message: String, templateIndex: Int, match: MatchResult): Component
    fun init()
}
