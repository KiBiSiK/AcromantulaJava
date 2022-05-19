package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaReferenceType
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol

/**
 * The class file name is a reference to the class name
 */
object ClassFileReference : AcromantulaReferenceType("java.class.file") {
    override fun onUpdateSymbolName(symbol: ContentMappingSymbol, reference: ContentMappingReference, newName: String) {
        // rename file
    }

    override fun stringRepresentation(ref: ContentMappingReference): String {
        return ref.file.name + ": FILE NAME " + ref.symbol.name
    }
}