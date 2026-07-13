package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import cloud.glitchdev.rfu.utils.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
    @Shadow @Final protected Minecraft minecraft;
    @Shadow @Final protected Connection connection;

    @Inject(method = "handleResourcePackPush", at = @At("HEAD"), cancellable = true)
    private void onHandleResourcePackPush(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (OtherSettings.INSTANCE.getAutoAcceptResourcePacks() && Network.INSTANCE.isOnHypixel()) {
            UUID packId = packet.id();
            String hash = packet.hash();
            String urlVal = packet.url();

            this.minecraft.execute(() -> {
                if (OtherSettings.INSTANCE.getSaveResourcePacks()) {
                    Path resourcePacksDir = this.minecraft.gameDirectory.toPath().resolve("resourcepacks");
                    String filename = "Hypixel Server Pack - " + packId + " - " + hash + ".zip";
                    Path destPack = resourcePacksDir.resolve(filename);
                    
                    String packNameInOptions = "file/" + filename;
                    boolean isSelected = this.minecraft.options.resourcePacks.contains(packNameInOptions);
                    
                    if (Files.exists(destPack)) {
                        if (!isSelected && OtherSettings.INSTANCE.getAutoLoadResourcePacks()) {
                            rfu$cleanUpOldVersionsInOptions(packId, filename);
                            this.minecraft.getResourcePackRepository().reload();
                            this.minecraft.options.resourcePacks.addFirst(packNameInOptions);
                            this.minecraft.options.loadSelectedResourcePacks(this.minecraft.getResourcePackRepository());
                            this.minecraft.options.save();
                            this.minecraft.reloadResourcePacks();
                        }

                        this.connection.send(new ServerboundResourcePackPacket(packId, Action.ACCEPTED));
                        this.connection.send(new ServerboundResourcePackPacket(packId, Action.DOWNLOADED));
                        this.connection.send(new ServerboundResourcePackPacket(packId, Action.SUCCESSFULLY_LOADED));
                        return;
                    }
                }

                URL url = rfu$parseResourcePackUrl(urlVal);
                if (url == null) {
                    this.connection.send(new ServerboundResourcePackPacket(packId, Action.INVALID_URL));
                } else {
                    this.minecraft.getDownloadedPackSource().allowServerPacks();
                    this.minecraft.getDownloadedPackSource().pushPack(packId, url, hash);
                }
            });
            ci.cancel();
        }
    }

    @Unique
    private void rfu$cleanUpOldVersionsInOptions(UUID packId, String currentFilename) {
        String prefix = "file/Hypixel Server Pack - " + packId + " - ";
        String currentOptionName = "file/" + currentFilename;
        this.minecraft.options.resourcePacks.removeIf(name -> name.startsWith(prefix) && !name.equals(currentOptionName));
    }

    @Unique
    private static URL rfu$parseResourcePackUrl(String urlString) {
        try {
            URL url = URI.create(urlString).toURL();
            String protocol = url.getProtocol();
            return !"http".equals(protocol) && !"https".equals(protocol) ? null : url;
        } catch (Exception e) {
            return null;
        }
    }
}
