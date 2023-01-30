package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol

abstract class JavaSymbol : AcromantulaSymbol {

    /**
     * Write symbol into corresponding database. This method is called from
     * [net.cydhra.acromantula.java.mapping.events.MappingDatabaseSync].
     */
    abstract fun writeIntoDatabase()
}