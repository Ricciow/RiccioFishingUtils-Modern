package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.LiquidPaletteSprite;
import cloud.glitchdev.rfu.feature.fishing.CustomLiquidTextures;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpriteContents.class)
public class SpriteContentsMixin implements LiquidPaletteSprite {
    @Shadow @Final private NativeImage originalImage;
    @Shadow @Final private MipmapStrategy mipmapStrategy;
    @Shadow @Final private float alphaCutoffBias;
    @Shadow private NativeImage[] byMipLevel;
    @Unique private CustomLiquidTextures.PaletteImage rfu$palette;
    @Unique private SpriteContents.AnimationState rfu$animation;

    @WrapOperation(
        method = "<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;computeTransparency()Lcom/mojang/blaze3d/platform/Transparency;")
    )
    private Transparency rfu$applyLiquidPalette(NativeImage image, Operation<Transparency> original, @Local(argsOnly = true, name = "name") Identifier name) {
        if ("rfu".equals(name.getNamespace()) && name.getPath().startsWith("block/custom_")) {
            this.rfu$palette = CustomLiquidTextures.INSTANCE.create(name, image);
            if (this.rfu$palette != null) this.rfu$palette.refresh(image);
        }
        return original.call(image);
    }

    @ModifyReturnValue(method = "createAnimationState", at = @At("RETURN"))
    private SpriteContents.AnimationState rfu$rememberAnimation(SpriteContents.AnimationState animation) {
        this.rfu$animation = animation;
        return animation;
    }

    @Override
    public NativeImage[] rfu$refreshPalette() {
        if (this.rfu$palette == null || !this.rfu$palette.refresh(this.originalImage)) return null;
        NativeImage[] updated = MipmapGenerator.generateMipLevels(
                ((SpriteContents) (Object) this).name(), new NativeImage[]{this.originalImage},
                this.byMipLevel.length - 1, this.mipmapStrategy, this.alphaCutoffBias, this.originalImage.computeTransparency());
        for (int level = 1; level < this.byMipLevel.length; level++) this.byMipLevel[level].close();
        this.byMipLevel = updated;
        return updated;
    }

    @Override
    public SpriteContents.AnimationState rfu$getAnimationState() {
        return this.rfu$animation;
    }
}