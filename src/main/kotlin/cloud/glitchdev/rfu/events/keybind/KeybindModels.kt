package cloud.glitchdev.rfu.events.keybind

//? if <26.3 {
/*import cloud.glitchdev.rfu.RiccioFishingUtils.mc
*///?}
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.managers.KeybindEvents
import cloud.glitchdev.rfu.utils.dsl.toInputKey
import com.mojang.blaze3d.platform.InputConstants

enum class KeyContext {
    IN_GAME,
    IN_GUI,
    CONTAINER_ONLY,
    ANY
}

enum class KeyModifier(val mask: Int) {
    SHIFT(InputConstants.MOD_SHIFT),
    CONTROL(InputConstants.MOD_CONTROL),
    ALT(InputConstants.MOD_ALT),
    SUPER(InputConstants.MOD_SUPER);

    companion object {
        fun fromModifiers(mods: Int): Set<KeyModifier> {
            val set = hashSetOf<KeyModifier>()
            if ((mods and InputConstants.MOD_SHIFT) != 0) set.add(SHIFT)
            if ((mods and InputConstants.MOD_CONTROL) != 0) set.add(CONTROL)
            if ((mods and InputConstants.MOD_ALT) != 0) set.add(ALT)
            if ((mods and InputConstants.MOD_SUPER) != 0) set.add(SUPER)
            return set
        }

        fun getCurrentModifiers(): Set<KeyModifier> {
            //? if <26.3 {
            /*val window = mc.window
            *///?}
            val set = hashSetOf<KeyModifier>()
            //~ if <26.3 'isKeyDown(' -> 'isKeyDown(window, ' {
            if (InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT)) {
                set.add(SHIFT)
            }
            if (InputConstants.isKeyDown(InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL)) {
                set.add(CONTROL)
            }
            if (InputConstants.isKeyDown(InputConstants.KEY_LALT) || InputConstants.isKeyDown(InputConstants.KEY_RALT)) {
                set.add(ALT)
            }
            //~ if <26.3 'KEY_LGUI' -> 'KEY_LSUPER' {
            //~ if <26.3 'KEY_RGUI' -> 'KEY_RSUPER' {
            if (InputConstants.isKeyDown(InputConstants.KEY_LGUI) || InputConstants.isKeyDown(InputConstants.KEY_RGUI)) {
                set.add(SUPER)
            }
            //~}
            //~}
            //~}
            return set
        }
    }
}

class KeybindTask(
    private val keyProvider: () -> Int,
    priority: Int = 20,
    val context: KeyContext = KeyContext.IN_GAME,
    val modifiers: Set<KeyModifier> = emptySet(),
    val matchModifiersStrict: Boolean = false,
    val consume: Boolean = false,
    val ignoreInTextFields: Boolean = true,
    val condition: (() -> Boolean)? = null,
    val onPress: (() -> Unit)? = null,
    val onRelease: (() -> Unit)? = null,
    val onHeld: (() -> Unit)? = null,
    val onRepeat: (() -> Unit)? = null,
    val name: String? = null
) : AbstractEventManager.ManagedTask<() -> Unit, KeybindTask>(priority, {}) {
    val key: Int get() = keyProvider()
    val inputKey: InputConstants.Key? get() = key.toInputKey()

    override fun register(): KeybindTask = KeybindEvents.submitTask(this)
    override fun unregister(): KeybindTask {
        if (KeybindEvents.activeTasks.remove(this)) {
            KeybindEvents.safeExecution { onRelease?.invoke() }
        }
        return KeybindEvents.removeTask(this)
    }
}

class KeybindBuilder {
    var key: () -> Int = { 0 }
    var priority: Int = 20
    var context: KeyContext = KeyContext.IN_GAME
    var modifiers: Set<KeyModifier> = emptySet()
    var matchModifiersStrict: Boolean = false
    var consume: Boolean = false
    var ignoreInTextFields: Boolean = true
    var condition: (() -> Boolean)? = null
    var onPress: (() -> Unit)? = null
    var onRelease: (() -> Unit)? = null
    var onHeld: (() -> Unit)? = null
    var onRepeat: (() -> Unit)? = null
    var name: String? = null

    fun key(keyCode: Int) { this.key = { keyCode } }
    fun key(keySupplier: () -> Int) { this.key = keySupplier }
    fun condition(block: () -> Boolean) { this.condition = block }
    fun onPress(block: () -> Unit) { this.onPress = block }
    fun onRelease(block: () -> Unit) { this.onRelease = block }
    fun onHeld(block: () -> Unit) { this.onHeld = block }
    fun onRepeat(block: () -> Unit) { this.onRepeat = block }

    fun build(): KeybindTask {
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
        )
    }
}
