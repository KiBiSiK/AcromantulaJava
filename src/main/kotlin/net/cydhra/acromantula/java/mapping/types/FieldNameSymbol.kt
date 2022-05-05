package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbolType
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.reconstructClassName
import net.cydhra.acromantula.java.util.reconstructFieldDefinition
import net.cydhra.acromantula.workspace.database.DatabaseMappingsManager
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import org.jetbrains.exposed.sql.transactions.transaction
import org.objectweb.asm.commons.Remapper

object FieldNameSymbol : AcromantulaSymbolType("java.field.name", true) {
    override fun onUpdateName(symbol: ContentMappingSymbol, newName: String) {
        val (classIdentity, fieldName, fieldDescriptor) = reconstructFieldDefinition(symbol.identifier.value)

        transaction {
            val allReferences = MapperFeature.getReferences(symbol)
            for (reference in allReferences) {
                MapperFeature.getReferenceType(reference.type).onUpdateSymbolName(symbol, reference, newName)
            }

            AsmRemappingHelper.remapScheduledFiles(
                symbol,
                FieldNameRemapper(reconstructClassName(classIdentity), fieldName, fieldDescriptor, newName)
            )

            DatabaseMappingsManager.updateSymbolName(symbol, newName)
            DatabaseMappingsManager.updateSymbolIdentifier(
                symbol,
                constructFieldIdentity(classIdentity, newName, fieldDescriptor)
            )
        }
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