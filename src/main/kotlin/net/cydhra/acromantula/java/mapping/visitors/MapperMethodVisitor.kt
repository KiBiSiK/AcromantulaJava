package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.types.FieldInstructionReferenceType
import net.cydhra.acromantula.java.mapping.types.MethodInstructionReferenceType
import net.cydhra.acromantula.java.mapping.types.TypeInstructionReferenceType
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * A method visitor that generates [net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference]s from
 * instructions within the method
 *
 * @param file the database file entity
 * @param owner the method symbol identifier that is used as the owning symbol
 */
class MapperMethodVisitor(private val file: FileEntity, private val owner: String) {
    suspend fun visitTypeInsn(opcode: Int, type: String) {
        val typeIdentity = constructClassIdentity(type)
        MapperFeature.insertReferenceIntoDatabase(TypeInstructionReferenceType, file, typeIdentity, this.owner, null)
    }

    suspend fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        val fieldIdentity = constructFieldIdentity(constructClassIdentity(owner), name, descriptor)
        MapperFeature.insertReferenceIntoDatabase(FieldInstructionReferenceType, file, fieldIdentity, this.owner, null)
    }

    suspend fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        val methodIdentity = constructMethodIdentity(owner, name, descriptor)
        MapperFeature.insertReferenceIntoDatabase(
            MethodInstructionReferenceType,
            file,
            methodIdentity,
            this.owner,
            null
        )
    }
}