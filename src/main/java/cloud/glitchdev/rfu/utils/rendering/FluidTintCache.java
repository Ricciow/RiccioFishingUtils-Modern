package cloud.glitchdev.rfu.utils.rendering;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public final class FluidTintCache {
    private final Map<Fluid, Entry> models = new ConcurrentHashMap<>();

    public FluidModel get(Fluid fluid, FluidModel source, @Nullable Color color, int opacity) {
        opacity = Math.clamp(opacity, 0, 255);
        if (color == null && opacity == 255) return source;

        Entry cached = this.models.get(fluid);
        if (cached != null && cached.source == source && Objects.equals(cached.color, color) && cached.opacity == opacity) {
            return cached.model;
        }

        BlockTintSource tint = color != null ? BlockTintSources.constant(color.getRGB()) : source.tintSource();
        if (opacity != 255) tint = new FluidOpacityTintSource(tint, opacity);
        FluidModel model = new FluidModel(source.layer(), source.stillMaterial(), source.flowingMaterial(), source.overlayMaterial(), tint);
        this.models.put(fluid, new Entry(source, color, opacity, model));
        return model;
    }

    private record Entry(FluidModel source, @Nullable Color color, int opacity, FluidModel model) {
    }
}
