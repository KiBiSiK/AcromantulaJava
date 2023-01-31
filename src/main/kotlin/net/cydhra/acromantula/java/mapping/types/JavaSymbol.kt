package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import org.jetbrains.exposed.dao.id.EntityID

abstract class JavaSymbol : AcromantulaSymbol {

    lateinit var databaseId: EntityID<Int>

    /**
     * Write symbol into corresponding database. This method is called from
     * [net.cydhra.acromantula.java.mapping.events.MappingDatabaseSync].
     */
    abstract fun writeIntoDatabase(): EntityID<Int>
}