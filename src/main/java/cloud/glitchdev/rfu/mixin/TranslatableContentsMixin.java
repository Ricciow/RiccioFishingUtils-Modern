package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;

@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {
    @ModifyVariable(method = "<init>(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static String rfu$modifyKey(String key) {
        return EmojiFeature.INSTANCE.replaceEmojis(key);
    }

    @ModifyVariable(method = "<init>(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Object[] rfu$modifyArgs(Object[] args) {
        if (args == null) return null;
        boolean changed = false;
        Object[] newArgs = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String original) {
                String replaced = EmojiFeature.INSTANCE.replaceEmojis(original);
                if (!Objects.equals(replaced, original)) {
                    newArgs[i] = replaced;
                    changed = true;
                    continue;
                }
            }
            newArgs[i] = arg;
        }

        return changed ? newArgs : args;
    }
}
