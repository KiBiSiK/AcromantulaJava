package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaReferenceType
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol

object ClassSuperReference : AcromantulaReferenceType("java.class.super") {
    override fun onUpdateSymbolName(symbol: ContentMappingSymbol, reference: ContentMappingReference, newName: String) {
        TODO("not implemented")
    }

    override fun stringRepresentation(ref: ContentMappingReference): String {
        return "${ref.file.name}: SUPERCLASS of ${ref.owner?.name}"
    }
}