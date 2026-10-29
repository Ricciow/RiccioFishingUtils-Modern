package cloud.glitchdev.rfu.utils.rendering;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@NullMarked
public final class FluidOpacityTintSource implements BlockTintSource {
    private final @Nullable BlockTintSource source;
    private final int opacity;

    public FluidOpacityTintSource(@Nullable BlockTintSource source, int opacity) {
        this.source = source;
        this.opacity = Math.clamp(opacity, 0, 255);
    }

    private int apply(int color) {
        int alpha = Math.round((color >>> 24) * this.opacity / 255f);
        return (color & 0xFFFFFF) | (alpha << 24);
    }

    @Override
    public int color(BlockState state) {
        return apply(this.source != null ? this.source.color(state) : -1);
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return apply(this.source != null ? this.source.colorInWorld(state, level, pos) : -1);
    }

    @Override
    public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return apply(this.source != null ? this.source.colorAsTerrainParticle(state, level, pos) : -1);
    }

    @Override
    public Set<Property<?>> relevantProperties() {
        return this.source != null ? this.source.relevantProperties() : Set.of();
    }
}
