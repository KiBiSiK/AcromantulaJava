package net.cydhra.acromantula.java.mapping.events

import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.workspace.WorkspaceService
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

/**
 * Event observer for [MappingEventBroker] that writes all entities into the workspace database
 */
class MappingDatabaseSync {

    fun onIdentifierAdded(event: JavaMappingEvent.AddedIdentifierEvent) {
        event.identifier.databaseId = WorkspaceService.databaseTransaction {
            val idPresent = JavaIdentifierTable.select { JavaIdentifierTable.identifier eq event.identifier.identifier }
                .singleOrNull()

            if (idPresent == null) {
                JavaIdentifierTable.insertAndGetId {
                    it[identifier] = event.identifier.identifier
                }
            } else {
                idPresent[JavaIdentifierTable.id]
            }
        }
    }

    fun onSymbolAdded(event: JavaMappingEvent.AddedSymbolEvent) {
        event.symbol.writeIntoDatabase()
    }

    fun onReferenceAdded(event: JavaMappingEvent.AddedReferenceEvent) {
        event.reference.writeReferenceToDatabase()
    }

}