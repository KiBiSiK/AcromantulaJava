package net.cydhra.acromantula.java.mapping.events

import net.cydhra.acromantula.workspace.util.EventBroker

class MappingEventBroker : EventBroker<JavaMappingEvent, MappingDatabaseSync>() {
    override suspend fun dispatchEvent(event: JavaMappingEvent) {

    }
}