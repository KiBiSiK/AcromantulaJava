package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbolType
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.java.util.reconstructClassName
import net.cydhra.acromantula.java.util.reconstructMethodDefinition
import net.cydhra.acromantula.workspace.database.DatabaseMappingsManager
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import org.jetbrains.exposed.sql.transactions.transaction
import org.objectweb.asm.commons.Remapper

object MethodNameSymbol : AcromantulaSymbolType("java.method.name", true) {
    override fun onUpdateName(symbol: ContentMappingSymbol, newName: String) {
        val (classIdentity, methodName, methodDescriptor) = reconstructMethodDefinition(symbol.identifier.value)

        transaction {
            val allReferences = MapperFeature.getReferences(symbol)
            for (reference in allReferences) {
                MapperFeature.getReferenceType(reference.type).onUpdateSymbolName(symbol, reference, newName)
            }

            AsmRemappingHelper.remapScheduledFiles(
                symbol,
                MethodNameRemapper(reconstructClassName(classIdentity), methodName, methodDescriptor, newName)
            )

            DatabaseMappingsManager.updateSymbolName(symbol, newName)
            DatabaseMappingsManager.updateSymbolIdentifier(
                symbol,
                constructMethodIdentity(classIdentity, newName, methodDescriptor)
            )
        }
    }

    /**
     * Remaps all references to a method to a new name
     */
    class MethodNameRemapper(
        private val owner: String,
        private val oldName: String,
        private val descriptor: String,
        private val newName: String
    ) : Remapper() {

        override fun mapMethodName(owner: String, name: String, descriptor: String): String {
            if (owner == this.owner && name == this.oldName && descriptor == this.descriptor) {
                return newName
            }

            return super.mapMethodName(owner, name, descriptor)
        }
    }
}