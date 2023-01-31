package net.cydhra.acromantula.java.mapping

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.cydhra.acromantula.features.mapper.MapperState
import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.events.JavaMappingEvent
import net.cydhra.acromantula.java.mapping.events.MappingDatabaseSync
import net.cydhra.acromantula.java.mapping.events.MappingEventBroker
import net.cydhra.acromantula.java.mapping.types.JavaSymbol
import java.util.*

/**
 * Context generated during mapping of imported java classes and archives. Holds the entire mapping until mapping
 * finishes, then dumps it into the database in the background
 *
 * @param identityCacheCapacity initial capacity of the set containing all java member identities
 */
class ClassMapperContext(identityCacheCapacity: Int) : MapperState {

    private val identities = Collections.synchronizedMap(HashMap<String, JavaIdentifier>(identityCacheCapacity))

    private val databaseSyncer = MappingEventBroker()

    init {
        databaseSyncer.registerObserver(MappingDatabaseSync())
    }

    fun addSymbol(symbol: JavaSymbol) {
        databaseSyncer.dispatch(JavaMappingEvent.AddedSymbolEvent(symbol))
    }

    fun addReference(identifier: String) {
        retrieveIdentifier(identifier)
    }

    /**
     * Add a new unique [JavaIdentifier] into the mapping. The identifier is being inserted into the database, or
     * synced with the database if it already exists in there.
     */
    fun retrieveIdentifier(identifier: String): JavaIdentifier {
        return identities.getOrPut(identifier) {
            JavaIdentifier(identifier).also { databaseSyncer.dispatch(JavaMappingEvent.AddedIdentifierEvent(it)) }
        }
    }

    override suspend fun onFinishMapping() {
        // shutdown the database syncer and suspend until it has emptied the queue
        withContext(Dispatchers.IO) {
            databaseSyncer.shutdown()
        }
    }
}