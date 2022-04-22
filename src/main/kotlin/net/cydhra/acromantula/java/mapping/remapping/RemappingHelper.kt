package net.cydhra.acromantula.java.mapping.remapping

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.database.DatabaseMappingsManager
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.jetbrains.exposed.sql.transactions.transaction
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
object RemappingHelper {

    /**
     * Search for all references to a [ContentMappingSymbol] and update them both in database as well as in bytecode.
     *
     * @param symbol the symbol being renamed
     * @param newName new name for the symbol
     * @param remapper the bytecode remapper to use for actual rewriting classes.
     */
    fun remapSymbolAndReferences(symbol: ContentMappingSymbol, newName: String, remapper: Remapper) {
        val allReferences = MapperFeature.getReferences(symbol)
        for (reference in allReferences) {
            updateBytecode(reference.file, remapper)
        }

        if (symbol.file != null) {
            updateBytecode(symbol.file!!, remapper)
            transaction {
                DatabaseMappingsManager.updateSymbolName(symbol, newName)
            }
        }
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