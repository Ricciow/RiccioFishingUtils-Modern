package cloud.glitchdev.rfu.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin {
    @Shadow protected abstract ClientItem.Properties getItemProperties(Identifier modelId);
    @Shadow protected abstract ItemModel getItemModel(Identifier modelId);

    @Inject(method = "appendItemLayers", at = @At("HEAD"), cancellable = true)
    private void onAppendItemLayers(
            ItemStackRenderState output,
            ItemStack item,
            ItemDisplayContext displayContext,
            @Nullable Level level,
            @Nullable ItemOwner owner,
            int seed,
            CallbackInfo ci
    ) {
        Identifier modelId = item.get(DataComponents.ITEM_MODEL);
        if (modelId != null) {
            ItemModel model = this.getItemModel(modelId);
            if (model instanceof MissingItemModel && !cloud.glitchdev.rfu.utils.ResourcePackUtils.isHypixelPackActive()) {
                Identifier defaultModelId = item.getItem().components().get(DataComponents.ITEM_MODEL);
                if (defaultModelId != null) {
                    modelId = defaultModelId;
                    model = this.getItemModel(modelId);
                }
            }
            output.setOversizedInGui(this.getItemProperties(modelId).oversizedInGui());
            model.update(output, item, (ItemModelResolver) (Object) this, displayContext, level instanceof ClientLevel clientLevel ? clientLevel : null, owner, seed);
        }
        ci.cancel();
    }
}
