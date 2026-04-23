package cloud.glitchdev.rfu.access;

import java.awt.Color;

public interface EntityAccess {
    void rfu$setGlowing(boolean glowing);
    boolean rfu$isGlowing();
    void rfu$setGlowColor(Color color);
    Color rfu$getGlowColor();
}
