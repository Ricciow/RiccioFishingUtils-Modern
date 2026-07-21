package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.utils.ResourcePackUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin {
    @Shadow protected abstract ItemModel getItemModel(Identifier modelId);

    @WrapOperation(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;getItemModel(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/item/ItemModel;"))
    private ItemModel onAppendItemLayers(ItemModelResolver instance, Identifier modelId, Operation<ItemModel> original, @Local ItemStack item) {
        ItemModel model = original.call(instance, modelId);
        if (model instanceof MissingItemModel && !ResourcePackUtils.isHypixelPackActive()) {
            model = this.getItemModel(item.getItem().components().get(DataComponents.ITEM_MODEL));
        }

        return model;
    }
}