package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager

/**
 * The class file name is a reference to the class name
 */
class ClassFileReference(
    referencedSymbol: AcromantulaSymbol,
    sourceFile: FileEntity
) : JavaReference(referencedSymbol, sourceFile) {

    override val referenceType: String
        get() = "java.class.file"

    override fun onUpdateSymbolName(newName: String) {
        val oldPath = referencedSymbol.name.substring(0, referencedSymbol.name.lastIndexOf("/") + 1)
        val newNameWithoutPath = newName.removePrefix(oldPath)

        if (newNameWithoutPath.contains('/')) {
            error("name contains path separators")
        }

        val newFileName = "$newNameWithoutPath.class"
        LogManager.getLogger().debug("renaming \"${referencedSymbol.sourceFile!!.name}\" to \"$newFileName\"")
        WorkspaceService.renameFileEntry(oldPath + referencedSymbol.sourceFile!!.name, newFileName)
    }

    override fun displayString(): String {
        TODO("not implemented")
    }
}