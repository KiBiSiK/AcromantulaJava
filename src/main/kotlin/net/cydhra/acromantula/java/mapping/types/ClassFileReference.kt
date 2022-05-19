package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaReferenceType
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import org.apache.logging.log4j.LogManager

/**
 * The class file name is a reference to the class name
 */
object ClassFileReference : AcromantulaReferenceType("java.class.file") {
    override fun onUpdateSymbolName(symbol: ContentMappingSymbol, reference: ContentMappingReference, newName: String) {
        val oldPath = symbol.name.substring(0, symbol.name.lastIndexOf("/") + 1)
        val newNameWithoutPath = newName.removePrefix(oldPath)

        if (newNameWithoutPath.contains('/')) {
            error("name contains path separators")
        }

        val newFileName = "$newNameWithoutPath.class"
        LogManager.getLogger().debug("renaming \"${symbol.file!!.name}\" to \"$newFileName\"")
        WorkspaceService.renameFileEntry(oldPath + symbol.file!!.name, newFileName)
    }

    override fun stringRepresentation(ref: ContentMappingReference): String {
        return ref.file.name + ": FILE NAME " + ref.symbol.name
    }
}