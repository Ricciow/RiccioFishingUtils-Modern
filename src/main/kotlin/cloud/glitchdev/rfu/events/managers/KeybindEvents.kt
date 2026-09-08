package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.keybind.KeyContext
import cloud.glitchdev.rfu.events.keybind.KeyModifier
import cloud.glitchdev.rfu.events.keybind.KeybindBuilder
import cloud.glitchdev.rfu.events.keybind.KeybindTask
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.dsl.toConfigCode
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import java.util.concurrent.ConcurrentHashMap

@AutoRegister
object KeybindEvents : AbstractEventManager<() -> Unit, KeybindTask>(), RegisteredEvent {
    override val runTasks: () -> Unit = {}
    val pressedKeys: MutableSet<Int> = ConcurrentHashMap.newKeySet()
    val activeTasks: MutableSet<KeybindTask> = ConcurrentHashMap.newKeySet()

    override fun register() {
        registerTickEvent { runHeldTasks() }
        registerDisconnectEvent {
            safeExecution {
                activeTasks.forEach { it.onRelease?.invoke() }
                activeTasks.clear()
                pressedKeys.clear()
            }
        }
    }

    fun isPressed(key: Int): Boolean {
        if (key == 0 || key == -1) return false
        return pressedKeys.contains(key) || (key == -99 && pressedKeys.contains(-102))
    }

    fun isPressed(keyProvider: () -> Int): Boolean = isPressed(keyProvider())
    fun isPressed(key: InputConstants.Key): Boolean = isPressed(key.toConfigCode())

    private fun getCurrentScreen(): Screen? {
        //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
        return mc.gui.screen()
        //~}
    }

    private fun isTextInputFocused(screen: Screen?): Boolean {
        val focused = screen?.focused ?: return false
        return focused is EditBox && focused.canConsumeInput()
    }

    private fun runHeldTasks() {
        if (pressedKeys.isEmpty()) return
        safeExecution {
            val currentScreen = getCurrentScreen()
            val textFocused = isTextInputFocused(currentScreen)
            val currentMods = KeyModifier.getCurrentModifiers()

            tasks.forEach { task ->
                val onHeld = task.onHeld ?: return@forEach
                val key = task.key
                if (!isPressed(key)) return@forEach
                if (task.ignoreInTextFields && textFocused) return@forEach
                if (!matchesContext(task.context, currentScreen)) return@forEach
                if (!matchesModifiers(task, currentMods)) return@forEach
                if (task.condition?.invoke() == false) return@forEach

                onHeld()
            }
        }
    }

    fun handleKeyEvent(keyCode: Int, action: Int, modifiers: Int): Boolean {
        if (keyCode == 0 || keyCode == -1) return false
        return dispatchInput(keyCode, action, modifiers)
    }

    fun handleMouseEvent(button: Int, action: Int, modifiers: Int): Boolean {
        if (button < 0) return false
        return dispatchInput(-(100 + button), action, modifiers)
    }

    private fun dispatchInput(code: Int, action: Int, modifiers: Int): Boolean {
        var consumed = false
        safeExecution {
            val currentScreen = getCurrentScreen()
            val textFocused = isTextInputFocused(currentScreen)
            val currentMods = KeyModifier.fromModifiers(modifiers)

            when (action) {
                1 -> pressedKeys.add(code)
                0 -> pressedKeys.remove(code)
            }

            if (action == 0) {
                val iter = activeTasks.iterator()
                while (iter.hasNext()) {
                    val task = iter.next()
                    val taskKey = task.key
                    if (taskKey == code || (code == -102 && taskKey == -99)) {
                        iter.remove()
                        task.onRelease?.invoke()
                        if (task.consume) consumed = true
                    }
                }
            } else if (action == 1 || action == 2) {
                tasks.forEach { task ->
                    val taskKey = task.key
                    if (taskKey == 0 || taskKey == -1) return@forEach
                    val keyMatches = taskKey == code || (code == -102 && taskKey == -99)
                    if (!keyMatches) return@forEach

                    if (task.ignoreInTextFields && textFocused) return@forEach
                    if (!matchesContext(task.context, currentScreen)) return@forEach
                    if (!matchesModifiers(task, currentMods)) return@forEach
                    if (task.condition?.invoke() == false) return@forEach

                    if (action == 1) {
                        activeTasks.add(task)
                        task.onPress?.invoke()
                    } else {
                        task.onRepeat?.invoke()
                    }

                    if (task.consume) consumed = true
                }
            }
        }
        return consumed
    }

    private fun matchesContext(context: KeyContext, screen: Screen?): Boolean {
        return when (context) {
            KeyContext.IN_GAME -> screen == null
            KeyContext.IN_GUI -> screen != null
            KeyContext.CONTAINER_ONLY -> screen is AbstractContainerScreen<*>
            KeyContext.ANY -> true
        }
    }

    private fun matchesModifiers(task: KeybindTask, currentMods: Set<KeyModifier>): Boolean {
        if (task.modifiers.isEmpty()) {
            return !task.matchModifiersStrict || currentMods.isEmpty()
        }
        return if (task.matchModifiersStrict) task.modifiers == currentMods else currentMods.containsAll(task.modifiers)
    }

    fun registerKeybind(
        key: () -> Int,
        priority: Int = 20,
        context: KeyContext = KeyContext.IN_GAME,
        modifiers: Set<KeyModifier> = emptySet(),
        matchModifiersStrict: Boolean = false,
        consume: Boolean = false,
        ignoreInTextFields: Boolean = true,
        condition: (() -> Boolean)? = null,
        onPress: (() -> Unit)? = null,
        onRelease: (() -> Unit)? = null,
        onHeld: (() -> Unit)? = null,
        onRepeat: (() -> Unit)? = null,
        name: String? = null
    ): KeybindTask {
        return KeybindTask(
            keyProvider = key,
            priority = priority,
            context = context,
            modifiers = modifiers,
            matchModifiersStrict = matchModifiersStrict,
            consume = consume,
            ignoreInTextFields = ignoreInTextFields,
            condition = condition,
            onPress = onPress,
            onRelease = onRelease,
            onHeld = onHeld,
            onRepeat = onRepeat,
            name = name
        ).register()
    }

    fun registerKeybind(
        key: Int,
        priority: Int = 20,
        context: KeyContext = KeyContext.IN_GAME,
        modifiers: Set<KeyModifier> = emptySet(),
        matchModifiersStrict: Boolean = false,
        consume: Boolean = false,
        ignoreInTextFields: Boolean = true,
        condition: (() -> Boolean)? = null,
        onPress: (() -> Unit)? = null,
        onRelease: (() -> Unit)? = null,
        onHeld: (() -> Unit)? = null,
        onRepeat: (() -> Unit)? = null,
        name: String? = null
    ): KeybindTask = registerKeybind(
        key = { key },
        priority = priority,
        context = context,
        modifiers = modifiers,
        matchModifiersStrict = matchModifiersStrict,
        consume = consume,
        ignoreInTextFields = ignoreInTextFields,
        condition = condition,
        onPress = onPress,
        onRelease = onRelease,
        onHeld = onHeld,
        onRepeat = onRepeat,
        name = name
    )

    fun registerKeybind(builder: KeybindBuilder.() -> Unit): KeybindTask =
        KeybindBuilder().apply(builder).build().register()
}
