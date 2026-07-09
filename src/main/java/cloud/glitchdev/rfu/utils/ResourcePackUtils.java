package cloud.glitchdev.rfu.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;

public class ResourcePackUtils {
    public static boolean isHypixelPackActive() {
        Minecraft mc = Minecraft.getInstance();
        for (Pack pack : mc.getResourcePackRepository().getSelectedPacks()) {
            String id = pack.getId();
            if (id.startsWith("server/") || id.startsWith("file/Hypixel Server Pack - ")) {
                return true;
            }
        }
        return false;
    }
}
