package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import cloud.glitchdev.rfu.constants.ui.TooltipGuiScale;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Matrix3x2fStack pose;
    @Shadow @Final private TextureAtlas guiSprites;

    @ModifyVariable(method = "tooltip", at = @At("HEAD"), name = "style", argsOnly = true)
    private Identifier rfu$modifyStyle(Identifier style) {
        if (style != null && this.guiSprites != null && !cloud.glitchdev.rfu.utils.ResourcePackUtils.isHypixelPackActive()) {
            Identifier bgSprite = style.withPath(path -> "tooltip/" + path + "_background");
            if (this.guiSprites.getSprite(bgSprite) == this.guiSprites.missingSprite()) {
                return null;
            }
        }
        return style;
    }

    @Unique
    private boolean rfu$isCustomScaleActive() {
        return OtherSettings.INSTANCE.getTooltipGuiScale() != TooltipGuiScale.DEFAULT;
    }

    @Unique
    private boolean rfu$isFixedCustomScaleActive() {
        TooltipGuiScale scaleSetting = OtherSettings.INSTANCE.getTooltipGuiScale();
        return scaleSetting != TooltipGuiScale.DEFAULT && scaleSetting != TooltipGuiScale.DYNAMIC;
    }

    @Unique
    private float rfu$getTooltipScaleFactor(Font font, List<ClientTooltipComponent> lines) {
        TooltipGuiScale scaleSetting = OtherSettings.INSTANCE.getTooltipGuiScale();
        if (scaleSetting == TooltipGuiScale.DEFAULT) {
            return 1.0f;
        }

        Window window = this.minecraft.getWindow();
        int currentGuiScale = window.getGuiScale();
        if (currentGuiScale <= 0) {
            return 1.0f;
        }

        if (scaleSetting == TooltipGuiScale.DYNAMIC) {
            int textWidth = 0;
            int tempHeight = lines.size() == 1 ? -2 : 0;
            for (ClientTooltipComponent line : lines) {
                int lineWidth = line.getWidth(font);
                if (lineWidth > textWidth) {
                    textWidth = lineWidth;
                }
                tempHeight += line.getHeight(font);
            }
            int totalWidth = textWidth + 24;
            int totalHeight = tempHeight + 24;
            int guiWidth = window.getGuiScaledWidth();
            int guiHeight = window.getGuiScaledHeight();
            float scale = 1.0f;
            if (totalWidth > guiWidth) {
                scale = Math.min(scale, (float) guiWidth / totalWidth);
            }
            if (totalHeight > guiHeight) {
                scale = Math.min(scale, (float) guiHeight / totalHeight);
            }
            return scale;
        }

        Integer scaleVal = scaleSetting.getScaleValue();
        if (scaleVal == null) {
            return 1.0f;
        }
        return (float) scaleVal / currentGuiScale;
    }

    @ModifyVariable(method = "tooltip", at = @At("HEAD"), name = "xo", argsOnly = true)
    private int rfu$modifyXo(int xo, Font font, List<ClientTooltipComponent> lines) {
        if (rfu$isFixedCustomScaleActive()) {
            return (int) (xo / rfu$getTooltipScaleFactor(font, lines));
        }
        return xo;
    }

    @ModifyVariable(method = "tooltip", at = @At("HEAD"), name = "yo", argsOnly = true)
    private int rfu$modifyYo(int yo, Font font, List<ClientTooltipComponent> lines) {
        if (rfu$isFixedCustomScaleActive()) {
            return (int) (yo / rfu$getTooltipScaleFactor(font, lines));
        }
        return yo;
    }

    @WrapOperation(method = "tooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiWidth()I"))
    private int rfu$guiWidth(GuiGraphicsExtractor instance, Operation<Integer> original, Font font, List<ClientTooltipComponent> lines) {
        if (rfu$isFixedCustomScaleActive()) {
            return (int) (original.call(instance) / rfu$getTooltipScaleFactor(font, lines));
        }
        return original.call(instance);
    }

    @WrapOperation(method = "tooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiHeight()I"))
    private int rfu$guiHeight(GuiGraphicsExtractor instance, Operation<Integer> original, Font font, List<ClientTooltipComponent> lines) {
        if (rfu$isFixedCustomScaleActive()) {
            return (int) (original.call(instance) / rfu$getTooltipScaleFactor(font, lines));
        }
        return original.call(instance);
    }

    @WrapOperation(
        method = "tooltip",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;")
    )
    private Vector2ic rfu$positionTooltip(
            ClientTooltipPositioner positioner,
            int screenWidth,
            int screenHeight,
            int xo,
            int yo,
            int textWidth,
            int tempHeight,
            Operation<Vector2ic> original,
            Font font,
            List<ClientTooltipComponent> lines
    ) {
        if (OtherSettings.INSTANCE.getTooltipGuiScale() == TooltipGuiScale.DYNAMIC) {
            float scale = rfu$getTooltipScaleFactor(font, lines);
            int offset = Math.max(0, Math.round(12 * scale) - 3);
            Vector2ic result = original.call(
                    positioner,
                    screenWidth,
                    screenHeight - offset,
                    xo,
                    yo,
                    Math.round(textWidth * scale),
                    Math.round(tempHeight * scale)
            );
            return new Vector2i(
                    Math.round(result.x() / scale),
                    Math.round(result.y() / scale)
            );
        }
        return original.call(positioner, screenWidth, screenHeight, xo, yo, textWidth, tempHeight);
    }

    @Inject(
        method = "tooltip",
        at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;", shift = At.Shift.AFTER)
    )
    private void rfu$scaleMatrix(
            Font font,
            List<ClientTooltipComponent> lines,
            int xo,
            int yo,
            ClientTooltipPositioner positioner,
            @Nullable Identifier style,
            CallbackInfo ci
    ) {
        if (rfu$isCustomScaleActive()) {
            float scale = rfu$getTooltipScaleFactor(font, lines);
            this.pose.scale(scale, scale);
        }
    }
}