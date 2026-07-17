package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.utils.ResourcePackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackReloadConfig;
import net.minecraft.client.resources.server.PackReloadConfig.IdAndPath;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(DownloadedPackSource.class)
public abstract class DownloadedPackSourceMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private static RepositorySource EMPTY_SOURCE;
    @Shadow private RepositorySource packSource;
    @Shadow private PackReloadConfig.Callbacks pendingReload;

    @Shadow public abstract void onReloadSuccess();

    @Inject(method = "startReload", at = @At("HEAD"), cancellable = true)
    private void onStartReload(PackReloadConfig.Callbacks callbacks, CallbackInfo ci) {
        if (callbacks.packsToLoad().isEmpty() && this.packSource == EMPTY_SOURCE) {
            this.pendingReload = callbacks;
            this.onReloadSuccess();
            ci.cancel();
        }
    }

    @Inject(method = "loadRequestedPacks", at = @At("HEAD"), cancellable = true)
    private void onLoadRequestedPacks(List<IdAndPath> packsToLoad, CallbackInfoReturnable<List<Pack>> cir) {
        if (ResourcePackUtils.onLoadRequestedPacks(this.minecraft, packsToLoad)) {
            // Return an empty list so Minecraft doesn't load it as a server resource pack
            cir.setReturnValue(new ArrayList<>());
        }
    }
}
