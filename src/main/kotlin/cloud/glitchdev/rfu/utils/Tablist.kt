package cloud.glitchdev.rfu.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry

object Tablist {
    val playerRegex = """^\[\d+\] ([^\s]+)(?: [^\s]+)?$""".toRegex()

    fun getTabListPlayers(): Collection<PlayerListEntry> {
        val client = MinecraftClient.getInstance()

        val networkHandler = client.networkHandler

        return networkHandler?.playerList ?: emptyList()
    }

    fun getTablistAsStrings() : List<String> {
        val players = getTabListPlayers()
        return players.mapNotNull { player ->
            val name = player.displayName?.string
            if (name.isNullOrEmpty()) null else name
        }
    }

    fun getPlayerNames() : List<String> {
        return getTablistAsStrings().mapNotNull { string ->
            playerRegex.find(string)?.groupValues?.getOrNull(1)
        }
    }
}