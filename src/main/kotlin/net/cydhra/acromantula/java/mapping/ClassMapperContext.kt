package net.cydhra.acromantula.java.mapping

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.features.mapper.MapperState
import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.events.JavaMappingEvent
import net.cydhra.acromantula.java.mapping.events.MappingDatabaseSync
import net.cydhra.acromantula.java.mapping.events.MappingEventBroker
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

    fun addSymbol(identifier: String, symbol: AcromantulaSymbol) {
        addJavaIdentifier(identifier)
    }

    fun addReference(identifier: String) {
        addJavaIdentifier(identifier)
    }

    /**
     * Add a new unique [JavaIdentifier] into the mapping. The identifier is being inserted into the database, or
     * synced with the database if it already exists in there.
     */
    private fun addJavaIdentifier(identifier: String): JavaIdentifier {
        return identities.getOrPut(identifier) {
            JavaIdentifier(identifier).also { databaseSyncer.dispatch(JavaMappingEvent.AddedIdentifierEvent(it)) }
        }
    }

    override suspend fun onFinishMapping() {
        // shutdown the database syncer and suspend until it has emptied the queue
        databaseSyncer.shutdown()
    }
}