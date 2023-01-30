package net.cydhra.acromantula.java.mapping.events

import net.cydhra.acromantula.workspace.util.EventBroker

class MappingEventBroker : EventBroker<JavaMappingEvent, MappingDatabaseSync>() {
    @Suppress("REDUNDANT_ELSE_IN_WHEN") // see else
    override suspend fun dispatchEvent(event: JavaMappingEvent) {
        when (event) {
            is JavaMappingEvent.AddedIdentifierEvent -> registeredObservers.forEach { it.onIdentifierAdded(event) }
            is JavaMappingEvent.AddedSymbolEvent -> registeredObservers.forEach { it.onSymbolAdded(event) }
            is JavaMappingEvent.AddedReferenceEvent -> registeredObservers.forEach { it.onReferenceAdded(event) }
            // this can never happen, though we add the exception here to crash when we forget to implement dispatch
            // for a new event type
            else -> throw AssertionError("unknown event dispatched")
        }
    }
}