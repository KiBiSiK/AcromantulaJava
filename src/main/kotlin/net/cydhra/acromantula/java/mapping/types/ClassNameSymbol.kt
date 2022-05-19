package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbolType
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.workspace.database.DatabaseMappingsManager
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import org.jetbrains.exposed.sql.transactions.transaction
import org.objectweb.asm.commons.Remapper

object ClassNameSymbol : AcromantulaSymbolType("java.class.name", true) {
    override fun onUpdateName(symbol: ContentMappingSymbol, newName: String) {
        val oldPath = symbol.name.substring(0, symbol.name.lastIndexOf("/") + 1)
        if (!newName.startsWith(oldPath) || newName.removePrefix(oldPath).contains('/')) {
            throw IllegalArgumentException("moving files is not yet supported")
        }

        transaction {
            val allReferences = MapperFeature.getReferences(symbol)
            for (reference in allReferences) {
                MapperFeature.getReferenceType(reference.type).onUpdateSymbolName(symbol, reference, newName)
            }

            AsmRemappingHelper.remapScheduledFiles(
                symbol,
                ClassNameRemapper(symbol.name, newName)
            )

            DatabaseMappingsManager.updateSymbolName(symbol, newName)
            DatabaseMappingsManager.updateSymbolIdentifier(
                symbol,
                constructClassIdentity(newName)
            )
        }
    }

    class ClassNameRemapper(
        private val oldName: String,
        private val newName: String
    ) : Remapper() {
        override fun mapType(internalName: String): String {
            if (internalName == oldName) {
                return newName
            }
            return super.mapType(internalName)
        }
    }
}