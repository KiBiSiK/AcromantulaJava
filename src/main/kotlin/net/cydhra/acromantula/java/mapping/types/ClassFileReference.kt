package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity

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
//        val oldPath = referencedSymbol.getName().substring(0, referencedSymbol.getName().lastIndexOf("/") + 1)
//        val newNameWithoutPath = newName.removePrefix(oldPath)
//
//        if (newNameWithoutPath.contains('/')) {
//            error("name contains path separators")
//        }
//
//        val newFileName = "$newNameWithoutPath.class"
//        LogManager.getLogger().debug("renaming \"${sourceFile.name}\" to \"$newFileName\"")
//        WorkspaceService.renameFileEntry(sourceFile, newFileName)
        TODO("not yet implemented")
    }

    override fun displayString(): String {
        TODO("not implemented")
    }

    override fun writeReferenceToDatabase() {
        TODO("Not yet implemented")
    }
}