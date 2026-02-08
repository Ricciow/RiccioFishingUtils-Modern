package cloud.glitchdev.rfu.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerInfo

object Tablist {
    val playerRegex = """^\[\d+\] ([^\s]+)(?: [^\s]+)?$""".toRegex()

    fun getTabListPlayers(): Collection<PlayerInfo> {
        val client = Minecraft.getInstance()

        val networkHandler = client.connection

        return networkHandler?.onlinePlayers ?: emptyList()
    }

    fun getTablistAsStrings() : List<String> {
        val players = getTabListPlayers()
        return players.mapNotNull { player ->
            val name = player.tabListDisplayName?.string
            if (name.isNullOrEmpty()) null else name
        }
    }

    fun getPlayerNames() : List<String> {
        return getTablistAsStrings().mapNotNull { string ->
            playerRegex.find(string)?.groupValues?.getOrNull(1)
        }
    }
}