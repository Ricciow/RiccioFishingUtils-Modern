package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.access.PlayerVisibilityRenderStateAccess
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.ui.VisiblePlayerEquipment
import cloud.glitchdev.rfu.events.managers.PlayerRenderStateEvents.registerPlayerRenderStateEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.utils.World
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.entity.layers.WingsLayer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Avatar
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

@RFUFeature
object HidePlayers : Feature {
    override fun onInitialize() {
        registerPlayerRenderStateEvent { player, state -> prepareRenderState(player, state) }
    }

    private fun prepareRenderState(player: Avatar, state: AvatarRenderState) {
        val visible = getVisibleEquipment(player)?.toSet()
        (state as PlayerVisibilityRenderStateAccess).`rfu$setVisiblePlayerEquipment`(visible)
        if (visible == null) return

        if (VisiblePlayerEquipment.NAME_TAG !in visible) {
            state.nameTag = null
            state.scoreText = null
        }
        if (VisiblePlayerEquipment.PLAYER_MODEL !in visible) {
            state.displayFireAnimation = false
            state.shadowPieces.clear()
        }

        if (VisiblePlayerEquipment.HELMET !in visible) {
            state.headEquipment = ItemStack.EMPTY
            state.headItem.clear()
            state.wornHeadType = null
        }
        if (VisiblePlayerEquipment.CHESTPLATE !in visible) state.chestEquipment = ItemStack.EMPTY
        if (VisiblePlayerEquipment.LEGGINGS !in visible) state.legsEquipment = ItemStack.EMPTY
        if (VisiblePlayerEquipment.BOOTS !in visible) state.feetEquipment = ItemStack.EMPTY
    }

    private fun getVisibleEquipment(player: Avatar): Array<out VisiblePlayerEquipment>? {
        val localPlayer = Minecraft.getInstance().player
        if (!World.isInSkyblock || localPlayer == null || player !is Player) return null

        val isFishing = FishingSession.isFishing && !FishingSession.isPaused
        val radius = OtherSettings.hideNearbyPlayersRadius
        if (player !== localPlayer && player.displayName.string.startsWith("§8[") &&
            OtherSettings.hideNearbyPlayers &&
            (!OtherSettings.hideNearbyPlayersOnlyWhenFishing || isFishing) &&
            localPlayer.distanceToSqr(player) <= radius.toDouble() * radius) {
            return OtherSettings.visiblePlayerEquipment
        }

        if (OtherSettings.hidePlayersGlobally) {
            return OtherSettings.globallyVisiblePlayerEquipment
        }
        return null
    }

    private fun getVisibleEquipment(state: EntityRenderState): Set<VisiblePlayerEquipment>? =
        (state as? PlayerVisibilityRenderStateAccess)?.`rfu$visiblePlayerEquipment`()

    fun shouldRenderModel(state: EntityRenderState): Boolean {
        val visible = getVisibleEquipment(state) ?: return true
        return VisiblePlayerEquipment.PLAYER_MODEL in visible
    }

    fun shouldRenderLayer(state: EntityRenderState, layer: RenderLayer<*, *>): Boolean {
        val visible = getVisibleEquipment(state) ?: return true
        return when (layer) {
            is HumanoidArmorLayer<*, *, *> -> true
            is CustomHeadLayer<*, *> -> VisiblePlayerEquipment.HELMET in visible
            is WingsLayer<*, *> -> VisiblePlayerEquipment.CHESTPLATE in visible
            is PlayerItemInHandLayer<*, *> -> VisiblePlayerEquipment.HELD_ITEMS in visible
            else -> VisiblePlayerEquipment.PLAYER_MODEL in visible
        }
    }
}
