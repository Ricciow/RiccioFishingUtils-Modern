package cloud.glitchdev.rfu.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.teamresourceful.resourcefulconfig.client.components.ModSprites;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ModSprites.class, remap = false)
public abstract class ModSpritesMixin {

    @WrapMethod(method = "ofIcon")
    private static Identifier rfu$useSocialIcon(String icon, Operation<Identifier> original) {
        return switch (icon) {
            case "rfu:emoji/discord", "rfu:emoji/github", "rfu:emoji/patreon" -> Identifier.parse(icon);
            default -> original.call(icon);
        };
    }
}
