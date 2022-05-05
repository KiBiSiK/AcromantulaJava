package net.cydhra.acromantula.java.mapping.remapping

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode

/**
 * Helper class that does the heavy lifting for class member remapping: Searches all references to a member from the
 * database, loads the classes from the workspace, triggers the remapping operation and updates all database entities
 * accordingly.
 * The actual remapping work in the class files is done by a [org.objectweb.asm.commons.Remapper] implementation.
 */
object AsmRemappingHelper {

    /**
     * A list of class files that are scheduled for remapping. They are being scheduled by the
     * [net.cydhra.acromantula.features.mapper.AcromantulaReferenceType] implementations. Once [remapScheduledFiles]
     * is called by the respective symbol type, all those class files are loaded, remapped and saved again. The list
     * is then cleared.
     */
    private val scheduledForRemapping = mutableSetOf<FileEntity>()

    /**
     * Schedule a reference for remapping. This method will store the reference's origin file in a list, and once
     * [remapScheduledFiles] is executed, all stored files are laoded, remapped and saved again.
     *
     * @param reference the reference that is being remapped
     */
    fun scheduleFileForRemapping(reference: ContentMappingReference) {
        scheduledForRemapping += reference.file
    }

    /**
     * Remap all previously stored files using the new symbol name
     *
     * @param symbol the symbol that is being renamed
     * @param remapper the ASM remapper implementation
     */
    fun remapScheduledFiles(symbol: ContentMappingSymbol, remapper: Remapper) {
        require(symbol.file != null) { "cannot remap external symbols" }

        updateBytecode(symbol.file!!, remapper)

        for (file in scheduledForRemapping) {
            updateBytecode(file, remapper)
        }

        scheduledForRemapping.clear()
    }

    /**
     * Load a class file, update it using the [remapper] and write it back
     */
    private fun updateBytecode(file: FileEntity, remapper: Remapper) {
        val content = WorkspaceService.getFileContent(file)
        val node = ClassNode()
        ClassReader(content).accept(node, ClassReader.EXPAND_FRAMES)

        val writer = ClassWriter(0)
        node.accept(ClassRemapper(writer, remapper))
        val remappedContent = writer.toByteArray()

        WorkspaceService.updateFileEntry(file, remappedContent)
    }
}