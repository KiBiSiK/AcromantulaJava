package net.cydhra.acromantula.java.mapping

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.features.mapper.MapperState
import java.util.*

/**
 * Context generated during mapping of imported java classes and archives. Holds the entire mapping until mapping
 * finishes, then dumps it into the database in the background
 *
 * @param identityCacheCapacity initial capacity of the set containing all java member identities
 */
class ClassMapperContext(identityCacheCapacity: Int) : MapperState {

    private val identities = Collections.synchronizedMap(HashMap<String, AcromantulaSymbol>(identityCacheCapacity))

    fun addSymbol(identity: String, symbol: AcromantulaSymbol) {
        if (!identities.containsKey(identity) || symbol.sourceFile != null) {
            identities[identity] = symbol
        }
    }

    fun addReference(identity: String) {

    }
}