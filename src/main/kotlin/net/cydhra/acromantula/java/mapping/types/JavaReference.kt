package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaReference
import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Common behavior of all reference types that java files define
 */
abstract class JavaReference(
    override val referencedSymbol: AcromantulaSymbol,
    override val sourceFile: FileEntity
) : AcromantulaReference {

    /**
     * Schedule the source file of this reference for remapping
     */
    override fun onUpdateSymbolName(newName: String) {
        AsmRemappingHelper.scheduleFileForRemapping(this)
    }

    /**
     * Write reference into corresponding database. This method is called from
     * [net.cydhra.acromantula.java.mapping.events.MappingDatabaseSync].
     */
    abstract fun writeReferenceToDatabase()
}