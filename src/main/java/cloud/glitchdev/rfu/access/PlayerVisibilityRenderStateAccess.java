package cloud.glitchdev.rfu.access;

import cloud.glitchdev.rfu.constants.ui.VisiblePlayerEquipment;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public interface PlayerVisibilityRenderStateAccess {
    @Nullable Set<VisiblePlayerEquipment> rfu$visiblePlayerEquipment();

    void rfu$setVisiblePlayerEquipment(@Nullable Set<VisiblePlayerEquipment> visible);
}
