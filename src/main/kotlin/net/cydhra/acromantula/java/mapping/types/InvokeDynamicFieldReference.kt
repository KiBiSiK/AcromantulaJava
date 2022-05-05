package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaReferenceType
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol

object InvokeDynamicFieldReference : AcromantulaReferenceType("java.insn.dynamic.method") {
    override fun onUpdateSymbolName(symbol: ContentMappingSymbol, reference: ContentMappingReference, newName: String) {
        AsmRemappingHelper.scheduleFileForRemapping(reference)
    }

    override fun stringRepresentation(ref: ContentMappingReference): String {
        return "${ref.file.name}: INVOKEDYNAMIC with argument [${ref.symbol.identifier}]"
    }
}