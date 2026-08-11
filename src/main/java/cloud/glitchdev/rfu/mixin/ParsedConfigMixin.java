package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.utils.BackupManager;
import cloud.glitchdev.rfu.utils.RFULogger;
import com.teamresourceful.resourcefulconfig.api.patching.ConfigPatchEvent;
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig;
import com.teamresourceful.resourcefulconfig.common.loader.Loader;
import com.teamresourceful.resourcefulconfig.common.loader.ParsedConfig;
import com.teamresourceful.resourcefulconfig.common.loader.Writer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(value = ParsedConfig.class, remap = false)
public abstract class ParsedConfigMixin {
    @Shadow public abstract String id();
    @Shadow public abstract int version();
    @Shadow private File getConfigFile() { throw new AssertionError(); }
    @Unique
    private final String rfu$settingsName = "rfu/settings";

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void onSave(CallbackInfo ci) {
        if (!rfu$settingsName.equals(this.id())) return;
        ci.cancel();

        try {
            ResourcefulConfig self = (ResourcefulConfig) this;
            String content = Writer.save(self).toString();
            File configFile = getConfigFile();

            BackupManager.INSTANCE.saveGzBackup("settings.jsonc.gz", content);
            BackupManager.INSTANCE.writeAtomically(configFile, content);
        } catch (Exception e) {
            RFULogger.warn("Failed to save config file " + id() + ".json", e);
        }
    }

    @Inject(method = "load", at = @At("HEAD"), cancellable = true)
    private void onLoad(Consumer<ConfigPatchEvent> handler, CallbackInfo ci) {
        if (!rfu$settingsName.equals(this.id())) return;
        ci.cancel();

        File file = getConfigFile();
        ParsedConfig self = (ParsedConfig) (Object) this;

        BackupManager.INSTANCE.loadOrRestoreSettings(
            file,
            id(),
            version(),
            handler,
            json -> {
                Loader.loadConfig(self, json);
                return null;
            }
        );

        if (file.getName().endsWith(".json") && !file.delete()) {
            RFULogger.warn("Failed to delete old config file " + id() + ".json");
        }
    }
}
