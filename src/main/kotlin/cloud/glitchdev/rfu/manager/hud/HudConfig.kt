package cloud.glitchdev.rfu.manager.hud

class HudConfig {
    var hudElements : MutableList<HudElement> = mutableListOf()

    fun getOrAdd(id : String, defaultX : Float = 0f, defaultY: Float = 0f, scale: Float = 1f) : HudElement {
        var element = hudElements.find { it.id == id }
        if(element == null) {
            element = HudElement(id, defaultX, defaultY, scale)
            hudElements.add(element)
        }
        return element
    }

    fun update(id: String, x: Float, y: Float, scale: Float) {
        val element = hudElements.find { it.id == id }

        if (element != null) {
            element.x = x
            element.y = y
            element.scale = scale
        } else {
            hudElements.add(HudElement(id, x, y, scale))
        }
    }

    class HudElement(
        val id : String,
        var x : Float = 0f,
        var y : Float = 0f,
        var scale : Float = 1f
    )
}