package cloud.glitchdev.rfu.party

import net.minecraft.network.chat.Component

enum class PartyCommandPermission {
    LEADER_ONLY,
    MEMBER_ONLY,
    SELF_TRIGGER,
    ANY
}

interface IPartyCommand {
    val name: String
    val aliases: List<String>
    val description: String
    val permission: PartyCommandPermission
    val responseTemplates: List<Pair<String, String>>

    fun execute(sender: String, args: List<String>)
    fun getRichResponse(sender: String, message: String, templateIndex: Int, match: MatchResult): Component
    fun init()
}
