package cloud.glitchdev.rfu.manager.hud

class HudConfig {
    var hudElements : MutableList<HudElement> = mutableListOf()

    fun getOrAdd(id : String, defaultX : Float = 0f, defaultY: Float = 0f, enabled: Boolean = false, scale: Float = 1f) : HudElement {
        var element = hudElements.find { it.id == id }
        if(element == null) {
            element = HudElement(id, defaultX, defaultY, enabled, scale)
            hudElements.add(element)
        }
        return element
    }

    fun update(id: String, x: Float, y: Float, enabled: Boolean, scale: Float) {
        val element = hudElements.find { it.id == id }

        if (element != null) {
            element.x = x
            element.y = y
            element.enabled = enabled
            element.scale = scale
        } else {
            hudElements.add(HudElement(id, x, y, enabled, scale))
        }
    }

    class HudElement(
        val id : String,
        var x : Float = 0f,
        var y : Float = 0f,
        var enabled : Boolean = false,
        var scale : Float = 1f
    )
}