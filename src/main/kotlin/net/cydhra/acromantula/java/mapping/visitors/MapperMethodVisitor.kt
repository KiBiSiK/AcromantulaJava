package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.java.mapping.JavaClassMapper
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.objectweb.asm.MethodVisitor

/**
 * A method visitor that generates [net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference]s from
 * instructions within the method
 *
 * @param file the database file entity
 * @param owner the method symbol that is used as the owning symbol
 */
class MapperMethodVisitor(private val file: FileEntity, private val owner: ContentMappingSymbol) :
    MethodVisitor(JavaClassMapper.ASM_VERSION) {
    override fun visitTypeInsn(opcode: Int, type: String?) {

    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {

    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {

    }
}