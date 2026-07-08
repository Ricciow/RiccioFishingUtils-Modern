package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import cloud.glitchdev.rfu.utils.network.Network;
import cloud.glitchdev.rfu.utils.RFULogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackReloadConfig.IdAndPath;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Mixin(DownloadedPackSource.class)
public abstract class DownloadedPackSourceMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "loadRequestedPacks", at = @At("HEAD"), cancellable = true)
    private void onLoadRequestedPacks(List<IdAndPath> packsToLoad, CallbackInfoReturnable<List<Pack>> cir) {
        if (OtherSettings.INSTANCE.getAutoAcceptResourcePacks() && 
            OtherSettings.INSTANCE.getSaveResourcePacks() && 
            Network.INSTANCE.isOnHypixel()) {
            
            try {
                Path resourcePacksDir = this.minecraft.gameDirectory.toPath().resolve("resourcepacks");
                if (!Files.exists(resourcePacksDir)) {
                    Files.createDirectories(resourcePacksDir);
                }

                for (IdAndPath pack : packsToLoad) {
                    String hash = rfu$getFileSHA1(pack.path());
                    String filename = "Hypixel Server Pack - " + pack.id() + " - " + hash + ".zip";
                    Path destPack = resourcePacksDir.resolve(filename);
                    
                    rfu$cleanUpOldVersions(pack.id(), filename);
                    
                    Files.copy(pack.path(), destPack, StandardCopyOption.REPLACE_EXISTING);

                    if (OtherSettings.INSTANCE.getAutoLoadResourcePacks()) {
                        String packNameInOptions = "file/" + filename;
                        this.minecraft.getResourcePackRepository().reload();
                        if (!this.minecraft.options.resourcePacks.contains(packNameInOptions)) {
                            this.minecraft.options.resourcePacks.addFirst(packNameInOptions);
                        }
                        this.minecraft.options.loadSelectedResourcePacks(this.minecraft.getResourcePackRepository());
                        this.minecraft.options.save();
                    }
                }
            } catch (IOException e) {
                RFULogger.INSTANCE.error("Failed to copy server resource pack to local resourcepacks folder", e, "[RFU]");
            }

            // Return an empty list so Minecraft doesn't load it as a server resource pack
            cir.setReturnValue(new ArrayList<>());
        }
    }

    @Unique
    private void rfu$cleanUpOldVersions(UUID packId, String currentFilename) {
        try {
            Path resourcePacksDir = this.minecraft.gameDirectory.toPath().resolve("resourcepacks");
            if (Files.exists(resourcePacksDir)) {
                String prefix = "Hypixel Server Pack - " + packId + " - ";
                try (Stream<Path> stream = Files.list(resourcePacksDir)) {
                    stream.forEach(path -> {
                        String name = path.getFileName().toString();
                        if (name.startsWith(prefix) && name.endsWith(".zip") && !name.equals(currentFilename)) {
                            String optionName = "file/" + name;
                            this.minecraft.options.resourcePacks.remove(optionName);
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                path.toFile().deleteOnExit();
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    @Unique
    private static String rfu$getFileSHA1(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream is = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            RFULogger.INSTANCE.error("Failed to read server resource pack file hash", e, "[RFU]");
            return "";
        }
    }
}
