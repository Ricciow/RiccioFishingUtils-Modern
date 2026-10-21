package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.events.managers.EntityFireRenderEvents.registerEntityFireRenderEvent
import cloud.glitchdev.rfu.events.managers.FluidRenderEvents.registerFluidRenderEvent
import cloud.glitchdev.rfu.events.managers.FogEvents.registerFogColorEvent
import cloud.glitchdev.rfu.events.managers.FogEvents.registerFinalFogEvent
import cloud.glitchdev.rfu.events.managers.FogEvents.registerCameraFluidEvent
import cloud.glitchdev.rfu.events.managers.FogEvents.registerWaterVisionFluidEvent
import cloud.glitchdev.rfu.events.managers.ScreenEffectEvents.registerFireOverlayEvent
import cloud.glitchdev.rfu.events.managers.ScreenEffectEvents.registerWaterOverlayEvent
import cloud.glitchdev.rfu.events.managers.ScreenEffectEvents.registerWaterOverlayAppearanceEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import net.minecraft.client.Camera
import net.minecraft.client.renderer.block.FluidModel
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.data.AtlasIds
import net.minecraft.resources.Identifier
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.Avatar
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.level.material.Fluids
import java.awt.Color

@RFUFeature
object CustomLava : Feature {
    private val grayscaleStill = Identifier.fromNamespaceAndPath("rfu", "block/lava_still_grayscale")
    private val grayscaleFlow = Identifier.fromNamespaceAndPath("rfu", "block/lava_flow_grayscale")
    private val neutralWaterOverlay = Identifier.fromNamespaceAndPath("rfu", "textures/misc/underwater_grayscale.png")

    @Volatile
    private var lavaModelCache: Pair<FluidModel, FluidModel>? = null

    override fun onInitialize() {
        registerFluidRenderEvent { context ->
            if (LavaFishing.customLava && context.fluidState.`is`(FluidTags.LAVA)) {
                if (LavaFishing.customLavaLiquid == LiquidTypes.WATER) {
                    context.model = context.models.get(Fluids.WATER.defaultFluidState())
                } else {
                    context.model = grayscaleModel(context.model)
                }
                context.color = Color(LavaFishing.customLavaColor)
            }
        }

        registerCameraFluidEvent { context ->
            if (usesWaterStyle() && context.state.`is`(FluidTags.LAVA)) {
                // Let Camera's water branch test the surface height; skip lava's near-plane detection.
                if (context.tag == FluidTags.WATER) context.matches = true
                else if (context.tag == FluidTags.LAVA) context.matches = false
            }
        }

        registerFogColorEvent { context ->
            if (LavaFishing.customLava && isInLava(context.camera) &&
                (context.environment is LavaFogEnvironment || context.environment is WaterFogEnvironment)) {
                context.color = Color(LavaFishing.customLavaColor)
                context.preserveBrightness = true
            }
        }

        registerFinalFogEvent { fog, camera, _, _, _ ->
            if (LavaFishing.customLava && LavaFishing.customLavaDisableFog && isInLava(camera)) {
                fog.environmentalStart = Float.MAX_VALUE
                fog.environmentalEnd = Float.MAX_VALUE
                fog.renderDistanceStart = Float.MAX_VALUE
                fog.renderDistanceEnd = Float.MAX_VALUE
                fog.skyEnd = Float.MAX_VALUE
                fog.cloudEnd = Float.MAX_VALUE
            }
        }

        registerWaterVisionFluidEvent { context ->
            if (usesWaterStyle() && context.player.isEyeInFluid(FluidTags.LAVA)) {
                context.submerged = true
            }
        }

        registerWaterOverlayEvent { context ->
            if (usesWaterStyle() && context.player.isEyeInFluid(FluidTags.LAVA)) context.visible = true
        }

        registerWaterOverlayAppearanceEvent { context ->
            if (usesWaterStyle() && context.player.isEyeInFluid(FluidTags.LAVA)) {
                context.color = Color(LavaFishing.customLavaColor)
                context.texture = neutralWaterOverlay
            }
        }

        registerFireOverlayEvent { context ->
            if (usesWaterStyle() || LavaFishing.customLava && LavaFishing.customLavaHideFire) context.visible = false
        }

        registerEntityFireRenderEvent { context ->
            if (usesWaterStyle() && (context.entity is Avatar || context.entity is FishingHook || context.entity is ItemEntity)) {
                context.visible = false
            }
        }

        var previousSettings = renderSettings()
        registerTickEvent { client ->
            val settings = renderSettings()
            if (settings != previousSettings) {
                val needsReload = previousSettings.first || settings.first
                previousSettings = settings
                if (needsReload) {
                    //~ if >=26.2 'levelRenderer' -> 'levelExtractor' {
                    client.levelExtractor.allChanged()
                    //~}
                }
            }
        }
    }

    private fun usesWaterStyle() = LavaFishing.customLava && LavaFishing.customLavaLiquid == LiquidTypes.WATER

    private fun isInLava(camera: Camera): Boolean {
        val level = mc.level ?: return false
        val pos = camera.blockPosition()
        val state = level.getFluidState(pos)
        //~ if >=26.3 'getHeight' -> 'getHeightForCamera' {
        return state.`is`(FluidTags.LAVA) && camera.position().y < pos.y + state.getHeightForCamera(level, pos)
        //~}
    }

    private fun grayscaleModel(source: FluidModel): FluidModel {
        val cached = lavaModelCache
        if (cached?.first === source) return cached.second

        val atlas = mc.atlasManager.getAtlasOrThrow(AtlasIds.BLOCKS)
        val model = FluidModel(
            source.layer(),
            Material.Baked(atlas.getSprite(grayscaleStill), source.stillMaterial().forceTranslucent()),
            Material.Baked(atlas.getSprite(grayscaleFlow), source.flowingMaterial().forceTranslucent()),
            source.overlayMaterial(),
            source.tintSource()
        )
        lavaModelCache = source to model
        return model
    }

    private fun renderSettings() = Triple(LavaFishing.customLava, LavaFishing.customLavaLiquid, LavaFishing.customLavaColor)
}
