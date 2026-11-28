package cloud.glitchdev.rfu.events.managers.player

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.connection.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.input.registerRawInputEvent
import cloud.glitchdev.rfu.events.managers.lifecycle.registerTickEvent
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.input.KeybindEvents

@AutoRegister
object AfkEvents : RegisteredEvent {
    const val AFK_TIMEOUT_MS = 5 * 60 * 1000L

    @Volatile
    var lastActivityTimeMs: Long = System.currentTimeMillis()
        private set

    var isAfk: Boolean = false
        private set

    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    private var lastYaw = 0f
    private var lastPitch = 0f

    override fun register(scope: EventScope) {
        scope.registerTickEvent(interval = 20) {
            if (mc.player == null || mc.level == null) return@registerTickEvent

            val mouseX = mc.mouseHandler.xpos()
            val mouseY = mc.mouseHandler.ypos()
            if (mouseX != lastMouseX || mouseY != lastMouseY) {
                lastMouseX = mouseX
                lastMouseY = mouseY
                recordActivity()
            }

            val player = mc.player
            if (player != null) {
                val yaw = player.yRot
                val pitch = player.xRot
                if (yaw != lastYaw || pitch != lastPitch) {
                    lastYaw = yaw
                    lastPitch = pitch
                    recordActivity()
                }
            }

            if (KeybindEvents.pressedKeys.isNotEmpty()) {
                recordActivity()
            }

            val now = System.currentTimeMillis()
            if (now - lastActivityTimeMs >= AFK_TIMEOUT_MS) {
                setAfk()
            }
        }

        scope.registerDisconnectEvent {
            isAfk = false
            lastActivityTimeMs = System.currentTimeMillis()
        }

        scope.registerRawInputEvent { _, _, _ ->
            recordActivity()
        }
    }

    fun recordActivity() {
        lastActivityTimeMs = System.currentTimeMillis()
        if (isAfk) {
            isAfk = false
            AfkStatusEventManager.runTasks(false)
        }
    }

    fun setAfk() {
        if (!isAfk) {
            isAfk = true
            AfkStatusEventManager.runTasks(true)
        }
    }

}
