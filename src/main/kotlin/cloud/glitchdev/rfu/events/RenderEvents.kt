package cloud.glitchdev.rfu.events

//? if >=1.21.10 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
*///?}

/**
 * Manages dynamic rendering tasks that execute during the World Render phase.
 *
 * Specifically hooks into [WorldRenderEvents.AFTER_ENTITIES], allowing custom
 * rendering (lines, shapes, overlays) to appear correctly depth-tested against
 * the world but drawn after standard entities.
 */
@AutoRegister
object RenderEvents : AbstractEventManager<(WorldRenderContext) -> Unit, RenderEvents.RenderEvent>() {

    override fun register() {
        WorldRenderEvents.AFTER_ENTITIES.register { client ->
            tasks.forEach { task ->
                task.callback(client)
            }
        }
    }

    /**
     * Helper factory to create and immediately register a new render event.
     *
     * @param priority The execution priority. Higher values run earlier (default: 20).
     * @param callback The logic to run. Returns `true` to run again next frame, `false` to destroy.
     * @return The registered [RenderEvent] instance.
     */
    fun registerRenderEvent(priority: Int = 20, callback: (WorldRenderContext) -> Unit): RenderEvent {
        return RenderEvent(priority, callback).register()
    }

    /**
     * A wrapper class for individual render tasks.
     *
     * @property priority Determines the order of execution relative to other tasks.
     * @property callback The function invoked every frame.
     * Input: [WorldRenderContext] for drawing.
     * Output: [Unit] - `true` to persist, `false` to unregister.
     */
    class RenderEvent(
        priority: Int = 20,
        callback: (WorldRenderContext) -> Unit
    ) : ManagedTask<(WorldRenderContext) -> Unit, RenderEvent>(priority, callback) {

        /** Submits this event to the central [RenderEvents] manager. */
        override fun register() = submitTask(this)

        /** Removes this event from the central [RenderEvents] manager. */
        override fun unregister() = removeTask(this)
    }
}