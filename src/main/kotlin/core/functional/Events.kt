package core.functional

interface DomainEvent {
    val eventId: String
    val occurredAt: String
    val aggregateId: String
}


interface EventPublisher {
    fun publish(event: DomainEvent)
    fun subscribe(eventType: String, handler: (DomainEvent) -> Unit)
}

class InMemoryEventBus : EventPublisher {
    private val subscribers = mutableMapOf<String, MutableList<(DomainEvent) -> Unit>>()

    override fun publish(event: DomainEvent) {
        val eventType = event::class.simpleName ?: return
        subscribers[eventType]?.forEach { handler ->
            try {
                handler(event)
            } catch (e: Exception) {
                println("Error handling event $eventType: ${e.message}")
            }
        }
    }

    override fun subscribe(eventType: String, handler: (DomainEvent) -> Unit) {
        subscribers.getOrPut(eventType) { mutableListOf() }.add(handler)
    }

}