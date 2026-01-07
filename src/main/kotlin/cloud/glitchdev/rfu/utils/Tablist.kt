package cloud.glitchdev.rfu.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry

object Tablist {
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
}