package cloud.glitchdev.rfu.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import kotlin.text.isEmpty

object Tablist {
    fun getTabListPlayers(): Collection<PlayerListEntry> {
        val client = MinecraftClient.getInstance()

        val networkHandler = client.networkHandler

        return networkHandler?.playerList ?: emptyList()
    }

    fun getTablistAsStrings() : List<String> {
        val players = getTabListPlayers()
        return players.map { it.displayName?.string }.filter { it != null && !it.isEmpty()} as List<String>
    }
}