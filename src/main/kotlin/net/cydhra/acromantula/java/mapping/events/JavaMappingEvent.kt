package net.cydhra.acromantula.java.mapping.events

sealed class JavaMappingEvent {

    class AddedIdentityEvent(identity: String) : JavaMappingEvent()

}