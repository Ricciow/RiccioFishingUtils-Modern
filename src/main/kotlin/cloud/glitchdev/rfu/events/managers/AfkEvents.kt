package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.KeybindEvents.registerRawInputEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent

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

    override fun register() {
        registerTickEvent(interval = 20) {
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

        registerDisconnectEvent {
            isAfk = false
            lastActivityTimeMs = System.currentTimeMillis()
        }

        registerRawInputEvent { _, _, _ ->
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

    fun registerAfkStatusChangedEvent(
        priority: Int = 20,
        callback: (isAfk: Boolean) -> Unit
    ): AfkStatusEventManager.AfkStatusEvent {
        return AfkStatusEventManager.register(priority, callback)
    }

    object AfkStatusEventManager : AbstractEventManager<(Boolean) -> Unit, AfkStatusEventManager.AfkStatusEvent>() {
        override val runTasks: (Boolean) -> Unit = { isAfk ->
            safeExecution {
                tasks.forEach { it.callback(isAfk) }
            }
        }

        fun register(priority: Int = 20, callback: (Boolean) -> Unit): AfkStatusEvent {
            return AfkStatusEvent(priority, callback).register()
        }

        class AfkStatusEvent(
            priority: Int = 20,
            callback: (Boolean) -> Unit
        ) : ManagedTask<(Boolean) -> Unit, AfkStatusEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
