package cloud.glitchdev.rfu.events

interface RegisteredEvent {
    fun register(scope: EventScope)
    fun postInitialize(scope: EventScope) {}
}