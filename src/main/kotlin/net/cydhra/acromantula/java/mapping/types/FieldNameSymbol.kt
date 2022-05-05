package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbolType
import net.cydhra.acromantula.java.mapping.remapping.RemappingHelper
import net.cydhra.acromantula.java.util.reconstructClassName
import net.cydhra.acromantula.java.util.reconstructFieldDefinition
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import org.objectweb.asm.commons.Remapper

object FieldNameSymbol : AcromantulaSymbolType("java.field.name", true) {
    override fun onUpdateName(symbol: ContentMappingSymbol, newName: String) {
        val (classIdentity, fieldName, fieldDescriptor) = reconstructFieldDefinition(symbol.name)
        RemappingHelper.remapSymbolAndReferences(
            symbol,
            newName,
            FieldNameRemapper(reconstructClassName(classIdentity), fieldName, fieldDescriptor, newName)
        )
    }

    /**
     * Remaps all references to a field to a new name
     */
    class FieldNameRemapper(
        private val owner: String,
        private val oldName: String,
        private val descriptor: String,
        private val newName: String
    ) : Remapper() {

        override fun mapFieldName(owner: String, name: String, descriptor: String): String {
            if (owner == this.owner && name == this.oldName && descriptor == this.descriptor) {
                return newName
            }

            return super.mapFieldName(owner, name, descriptor)
        }
    }
}