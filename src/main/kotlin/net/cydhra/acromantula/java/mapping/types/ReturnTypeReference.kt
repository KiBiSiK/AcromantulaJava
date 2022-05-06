package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaReferenceType
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol

object ReturnTypeReference : AcromantulaReferenceType("java.method.return") {
    override fun onUpdateSymbolName(symbol: ContentMappingSymbol, reference: ContentMappingReference, newName: String) {
        TODO("not implemented")
    }

    override fun stringRepresentation(ref: ContentMappingReference): String {
        // TODO: update
        return "${ref.file.name}: METHOD RETURN TYPE for ${ref.owner?.name}"
    }
}