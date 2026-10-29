package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.access.CameraFluidStateAccess
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.events.managers.CloseConfigEvents.registerCloseConfigEvent
import cloud.glitchdev.rfu.events.managers.EntityFireRenderEvents.registerEntityFireRenderEvent
import cloud.glitchdev.rfu.events.managers.FluidRenderEvents.registerFluidRenderEvent
import cloud.glitchdev.rfu.events.managers.FogEvents.registerFogColorEvent
import cloud.glitchdev.rfu.events.managers.FogEvents.registerFinalFogEvent
import cloud.glitchdev.rfu.events.managers.FogEvents.registerCameraFluidEvent
import cloud.glitchdev.rfu.events.managers.FogEvents.registerWaterVisionFluidEvent
import cloud.glitchdev.rfu.events.managers.ScreenEffectEvents.registerFireOverlayEvent
import cloud.glitchdev.rfu.events.managers.ScreenEffectEvents.registerWaterOverlayEvent
import cloud.glitchdev.rfu.events.managers.ScreenEffectEvents.registerWaterOverlayAppearanceEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import net.minecraft.client.Camera
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.block.FluidModel
import net.minecraft.client.renderer.chunk.ChunkSectionLayer
import net.minecraft.client.renderer.fog.FogData
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.data.AtlasIds
import net.minecraft.resources.Identifier
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.Avatar
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import java.awt.Color

@RFUFeature
object CustomLiquids : Feature {
    private val neutralWaterOverlay = Identifier.fromNamespaceAndPath("rfu", "textures/misc/underwater_grayscale.png")

    private data class ModelCache(val source: FluidModel, val translucent: Boolean, val model: FluidModel)
    private data class RenderSettings(val enabled: Boolean, val style: LiquidTypes, val tint: Color?, val opacity: Int)

    @Volatile
    private var lavaModelCache: ModelCache? = null
    @Volatile
    private var waterModelCache: ModelCache? = null

    override fun onInitialize() {
        registerFluidRenderEvent { context ->
            val liquid = context.fluidState.customLiquid() ?: return@registerFluidRenderEvent
            val style = liquid.customStyle
            val source = if (liquid == style) context.model else context.getBaseModel(
                if (style == LiquidTypes.WATER) Fluids.WATER.defaultFluidState() else Fluids.LAVA.defaultFluidState()
            )
            context.model = styledModel(source, liquid, style)
            context.color = if (style == LiquidTypes.WATER) liquid.customColor else null
            context.opacity = CustomLiquidTextures.opacity(liquid, style)
        }

        registerCameraFluidEvent { context ->
            val liquid = context.state.customLiquid() ?: return@registerCameraFluidEvent
            context.disableFog = liquid.disableFog ||
                (liquid.customStyle == LiquidTypes.LAVA && CustomLiquidTextures.opacity(liquid, LiquidTypes.LAVA) == 0)
            if (context.tag == FluidTags.WATER) {
                context.matches = liquid.customStyle == LiquidTypes.WATER
            } else if (context.tag == FluidTags.LAVA) {
                context.matches = liquid.customStyle == LiquidTypes.LAVA
            }
        }

        registerFogColorEvent { context ->
            val liquid = customLiquidAt(context.camera) ?: return@registerFogColorEvent
            val color = liquid.customColor ?: return@registerFogColorEvent
            if (context.environment is LavaFogEnvironment || context.environment is WaterFogEnvironment) {
                context.color = color
                context.preserveBrightness = true
            }
        }

        registerFinalFogEvent { fog, camera, _, _, _ ->
            val liquid = customLiquidAt(camera) ?: return@registerFinalFogEvent
            if (liquid.disableFog) {
                applyFogOpacity(fog, 0)
            } else if (liquid.customStyle == LiquidTypes.LAVA) {
                applyFogOpacity(fog, CustomLiquidTextures.opacity(liquid, LiquidTypes.LAVA))
            }
        }

        registerWaterVisionFluidEvent { context ->
            val liquid = customLiquidAtEyes(context.player) ?: return@registerWaterVisionFluidEvent
            context.submerged = liquid.customStyle == LiquidTypes.WATER
        }

        registerWaterOverlayEvent { context ->
            val liquid = customLiquidAtEyes(context.player) ?: return@registerWaterOverlayEvent
            context.visible = liquid.customStyle == LiquidTypes.WATER
        }

        registerWaterOverlayAppearanceEvent { context ->
            val liquid = customLiquidAtEyes(context.player) ?: return@registerWaterOverlayAppearanceEvent
            val color = liquid.customColor ?: return@registerWaterOverlayAppearanceEvent
            if (liquid.customStyle == LiquidTypes.WATER) {
                context.color = color
                context.texture = neutralWaterOverlay
            }
        }

        registerFireOverlayEvent { context ->
            if (hideFireEffects()) context.visible = false
        }

        registerEntityFireRenderEvent { context ->
            if (hideFireEffects() && (context.entity is Avatar || context.entity is FishingHook || context.entity is ItemEntity)) {
                context.visible = false
            }
        }

        var previousSettings = renderSettings()
        registerCloseConfigEvent {
            val settings = renderSettings()
            val needsRebuild = previousSettings.zip(settings).any { (previous, current) ->
                previous != current && (previous.enabled || current.enabled)
            }
            previousSettings = settings
            if (needsRebuild) {
                //~ if >=26.2 'levelRenderer' -> 'levelExtractor' {
                mc.levelExtractor.allChanged()
                //~}
            }
        }
    }

    private fun applyFogOpacity(fog: FogData, opacity: Int) {
        val alpha = opacity.coerceIn(0, 255) / 255f
        fog.color.w *= alpha
        if (alpha == 0f) {
            fog.environmentalStart = Float.MAX_VALUE
            fog.environmentalEnd = Float.MAX_VALUE
            fog.renderDistanceStart = Float.MAX_VALUE
            fog.renderDistanceEnd = Float.MAX_VALUE
            fog.skyEnd = Float.MAX_VALUE
            fog.cloudEnd = Float.MAX_VALUE
        }
    }

    private fun hideFireEffects() = LavaFishing.customLava && LavaFishing.customLavaHideFire

    private fun customLiquidAt(camera: Camera): LiquidTypes? =
        (camera as CameraFluidStateAccess).`rfu$getFogFluidState`()?.customLiquid()

    private fun customLiquidAtEyes(player: LocalPlayer): LiquidTypes? = when {
        player.isEyeInFluid(FluidTags.WATER) && GeneralFishing.customWater -> LiquidTypes.WATER
        player.isEyeInFluid(FluidTags.LAVA) && LavaFishing.customLava -> LiquidTypes.LAVA
        else -> null
    }

    private fun FluidState.customLiquid(): LiquidTypes? = when {
        `is`(FluidTags.WATER) && GeneralFishing.customWater -> LiquidTypes.WATER
        `is`(FluidTags.LAVA) && LavaFishing.customLava -> LiquidTypes.LAVA
        else -> null
    }

    private val LiquidTypes.customStyle: LiquidTypes
        get() = if (this == LiquidTypes.WATER) GeneralFishing.customWaterLiquid else LavaFishing.customLavaLiquid

    private val LiquidTypes.customColor: Color?
        get() = when {
            this == LiquidTypes.LAVA && !LavaFishing.customLavaCustomColor -> null
            this == LiquidTypes.LAVA && LavaFishing.customLavaLiquid == LiquidTypes.LAVA -> Color(LavaFishing.customLavaMidtoneColor)
            this == LiquidTypes.LAVA -> Color(LavaFishing.customLavaColor)
            GeneralFishing.customWaterCustomColor && GeneralFishing.customWaterLiquid == LiquidTypes.LAVA ->
                Color(GeneralFishing.customWaterMidtoneColor)
            GeneralFishing.customWaterCustomColor -> Color(GeneralFishing.customWaterColor)
            else -> null
        }

    private val LiquidTypes.disableFog: Boolean
        get() = if (this == LiquidTypes.WATER) GeneralFishing.customWaterDisableFog else LavaFishing.customLavaDisableFog

    private fun styledModel(source: FluidModel, owner: LiquidTypes, style: LiquidTypes): FluidModel {
        val translucent = CustomLiquidTextures.opacity(owner, style) < 255
        val cached = if (owner == LiquidTypes.WATER) waterModelCache else lavaModelCache
        if (cached?.source === source && cached.translucent == translucent) return cached.model

        val atlas = mc.atlasManager.getAtlasOrThrow(AtlasIds.BLOCKS)
        val model = FluidModel(
            if (translucent) ChunkSectionLayer.TRANSLUCENT else source.layer(),
            Material.Baked(atlas.getSprite(CustomLiquidTextures.spriteId(owner, style, "still")), translucent || source.stillMaterial().forceTranslucent()),
            Material.Baked(atlas.getSprite(CustomLiquidTextures.spriteId(owner, style, "flow")), translucent || source.flowingMaterial().forceTranslucent()),
            source.overlayMaterial()?.let {
                Material.Baked(atlas.getSprite(CustomLiquidTextures.spriteId(owner, style, "overlay")), translucent || it.forceTranslucent())
            },
            source.tintSource()
        )
        val updated = ModelCache(source, translucent, model)
        if (owner == LiquidTypes.WATER) waterModelCache = updated else lavaModelCache = updated
        return model
    }

    private fun renderSettings() = LiquidTypes.entries.map { liquid ->
        val style = liquid.customStyle
        RenderSettings(
            if (liquid == LiquidTypes.WATER) GeneralFishing.customWater else LavaFishing.customLava,
            style,
            if (style == LiquidTypes.WATER) liquid.customColor else null,
            CustomLiquidTextures.opacity(liquid, style)
        )
    }
}
