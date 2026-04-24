package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.UKeyboard
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

@AutoRegister
object KeyboardEvents : AbstractEventManager<() -> Unit, KeyboardEvents.KeyboardEvent>(), RegisteredEvent {
    private val keyStates = mutableMapOf<Int, Boolean>()

    override fun register() {
        registerTickEvent {
            runTasks()
        }
    }

    override val runTasks: () -> Unit = {
        safeExecution {
            tasks.forEach { task ->
                val key = task.key
                if (key == 0) return@forEach
                
                val isDown = UKeyboard.isKeyDown(key) && mc.screen == null
                val wasDown = keyStates.getOrDefault(key, false)

                if (isDown && !wasDown) {
                    task.onPress?.invoke()
                } else if (!isDown && wasDown) {
                    task.onRelease?.invoke()
                }
                
                if (isDown) {
                    task.onHeld?.invoke()
                }

                keyStates[key] = isDown
            }
        }
    }

    fun registerKeyboardEvent(
        key: () -> Int,
        priority: Int = 20,
        onPress: (() -> Unit)? = null,
        onRelease: (() -> Unit)? = null,
        onHeld: (() -> Unit)? = null
    ): KeyboardEvent {
        return KeyboardEvent(key, priority, onPress, onRelease, onHeld).register()
    }

    fun registerKeyboardEvent(
        key: Int,
        priority: Int = 20,
        onPress: (() -> Unit)? = null,
        onRelease: (() -> Unit)? = null,
        onHeld: (() -> Unit)? = null
    ) : KeyboardEvent {
        return KeyboardEvent({ key }, priority, onPress, onRelease, onHeld).register()
    }

    class KeyboardEvent(
        private val keyProvider: () -> Int,
        priority: Int,
        val onPress: (() -> Unit)?,
        val onRelease: (() -> Unit)?,
        val onHeld: (() -> Unit)?
    ) : ManagedTask<() -> Unit, KeyboardEvent>(priority, {}) {
        val key: Int get() = keyProvider()
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
