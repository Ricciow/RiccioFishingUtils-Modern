package cloud.glitchdev.rfu.mixin;

//~ if >=26.3 'com.mojang.blaze3d.textures' -> 'com.mojang.renderpearl.api.textures' {
import com.mojang.renderpearl.api.textures.GpuTextureView;
//~}
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.AnimationState.class)
public interface SpriteAnimationStateAccessor {
    @Accessor("frameTexturesByIndex")
    Int2ObjectMap<GpuTextureView> rfu$getFrameTextures();

    @Accessor("isDirty")
    void rfu$setDirty(boolean dirty);
}