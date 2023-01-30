package net.cydhra.acromantula.java.mapping.events

import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.types.JavaReference
import net.cydhra.acromantula.java.mapping.types.JavaSymbol

sealed class JavaMappingEvent {

    class AddedIdentifierEvent(val identifier: JavaIdentifier) : JavaMappingEvent()

    class AddedSymbolEvent(val symbol: JavaSymbol) : JavaMappingEvent()

    class AddedReferenceEvent(val reference: JavaReference) : JavaMappingEvent()
}