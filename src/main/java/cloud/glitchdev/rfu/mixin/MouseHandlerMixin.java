package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.KeybindEvents;
import cloud.glitchdev.rfu.gui.window.HudWindow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private double xpos;
    @Shadow private double ypos;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void rfu$onMouseButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (KeybindEvents.INSTANCE.handleMouseEvent(rawButtonInfo.button(), action, rawButtonInfo.modifiers())) {
            ci.cancel();
            return;
        }

        if (HudWindow.INSTANCE.getCurrentContainerScreen() != null) {
            Window window = this.minecraft.getWindow();
            if (handle == window.handle()) {
                double xm = MouseHandler.getScaledXPos(window, this.xpos);
                double ym = MouseHandler.getScaledYPos(window, this.ypos);
                if (action == 1) {
                    if (HudWindow.INSTANCE.handleMouseClicked(xm, ym, rawButtonInfo.button())) {
                        ci.cancel();
                    }
                } else if (action == 0) {
                    if (HudWindow.INSTANCE.handleMouseReleased(xm, ym, rawButtonInfo.button())) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void rfu$onMouseScrolled(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (HudWindow.INSTANCE.getCurrentContainerScreen() != null) {
            Window window = this.minecraft.getWindow();
            if (handle == window.handle()) {
                boolean discreteScroll = this.minecraft.options.discreteMouseScroll().get();
                double scrollSensitivity = this.minecraft.options.mouseWheelSensitivity().get();
                double scaledXOffset = (discreteScroll ? Math.signum(xoffset) : xoffset) * scrollSensitivity;
                double scaledYOffset = (discreteScroll ? Math.signum(yoffset) : yoffset) * scrollSensitivity;
                double xm = MouseHandler.getScaledXPos(window, this.xpos);
                double ym = MouseHandler.getScaledYPos(window, this.ypos);
                if (HudWindow.INSTANCE.handleMouseScrolled(xm, ym, scaledXOffset, scaledYOffset)) {
                    ci.cancel();
                }
            }
        }
    }

    @WrapOperation(
        method = "handleAccumulatedMovement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"
        )
    )
    private boolean rfu$wrapMouseDragged(Screen instance, MouseButtonEvent event, double dx, double dy, Operation<Boolean> original) {
        if (instance instanceof AbstractContainerScreen) {
            if (HudWindow.INSTANCE.handleMouseDragged(event.x(), event.y(), event.button(), dx, dy)) {
                return true;
            }
        }
        return original.call(instance, event, dx, dy);
    }
}
