package cloud.glitchdev.rfu.access;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;

public interface LiquidPaletteSprite {
    NativeImage[] rfu$refreshPalette();
    SpriteContents.AnimationState rfu$getAnimationState();
}