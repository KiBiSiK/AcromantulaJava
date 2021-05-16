package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * A method visitor that generates [net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference]s from
 * instructions within the method
 *
 * @param file the database file entity
 * @param owner the method symbol that is used as the owning symbol
 */
class MapperMethodVisitor(private val file: FileEntity, private val owner: ContentMappingSymbol) {
    suspend fun visitTypeInsn(opcode: Int, type: String?) {

    }

    suspend fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {

    }

    suspend fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {

    }
}