package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.access.NearbyPlayerRenderStateAccess
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.ui.VisiblePlayerEquipment
import cloud.glitchdev.rfu.events.managers.PlayerRenderStateEvents.registerPlayerRenderStateEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
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
object HideNearbyPlayers : Feature {
    override fun onInitialize() {
        registerPlayerRenderStateEvent { player, state -> prepareRenderState(player, state) }
    }

    private fun prepareRenderState(player: Avatar, state: AvatarRenderState) {
        val localPlayer = Minecraft.getInstance().player
        val radius = OtherSettings.hideNearbyPlayersRadius
        val hidden =
                World.isInSkyblock && OtherSettings.hideNearbyPlayers && localPlayer != null &&
                player is Player && player !== localPlayer &&
                localPlayer.distanceToSqr(player) <= radius.toDouble() * radius &&
                player.displayName.string.startsWith("§8[")
        (state as NearbyPlayerRenderStateAccess).`rfu$setHideNearbyPlayer`(hidden)
        if (!hidden) return

        val visible = OtherSettings.visiblePlayerEquipment
        if (VisiblePlayerEquipment.NAME_TAG !in visible) {
            state.nameTag = null
            state.scoreText = null
        }
        if (VisiblePlayerEquipment.PLAYER_MODEL !in visible) state.displayFireAnimation = false

        if (VisiblePlayerEquipment.HELMET !in visible) {
            state.headEquipment = ItemStack.EMPTY
            state.headItem.clear()
            state.wornHeadType = null
        }
        if (VisiblePlayerEquipment.CHESTPLATE !in visible) state.chestEquipment = ItemStack.EMPTY
        if (VisiblePlayerEquipment.LEGGINGS !in visible) state.legsEquipment = ItemStack.EMPTY
        if (VisiblePlayerEquipment.BOOTS !in visible) state.feetEquipment = ItemStack.EMPTY
    }

    fun isHidden(state: EntityRenderState): Boolean =
        state is NearbyPlayerRenderStateAccess && state.`rfu$hideNearbyPlayer`()

    fun shouldRenderModel(state: EntityRenderState): Boolean =
        !isHidden(state) || VisiblePlayerEquipment.PLAYER_MODEL in OtherSettings.visiblePlayerEquipment

    fun shouldRenderLayer(state: EntityRenderState, layer: RenderLayer<*, *>): Boolean {
        if (!isHidden(state)) return true
        val visible = OtherSettings.visiblePlayerEquipment
        return when (layer) {
            is HumanoidArmorLayer<*, *, *> -> true
            is CustomHeadLayer<*, *> -> VisiblePlayerEquipment.HELMET in visible
            is WingsLayer<*, *> -> VisiblePlayerEquipment.CHESTPLATE in visible
            is PlayerItemInHandLayer<*, *> -> VisiblePlayerEquipment.HELD_ITEMS in visible
            else -> VisiblePlayerEquipment.PLAYER_MODEL in visible
        }
    }
}