package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.types.*
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
        MapperFeature.insertSymbolIntoDatabase(ClassNameSymbolType, null, typeIdentity, type, null)
        MapperFeature.insertReferenceIntoDatabase(TypeInstructionReferenceType, file, typeIdentity, this.owner, null)
    }

    suspend fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        val fieldIdentity = constructFieldIdentity(constructClassIdentity(owner), name, descriptor)
        MapperFeature.insertSymbolIntoDatabase(FieldNameSymbolType, null, fieldIdentity, name, null)
        MapperFeature.insertSymbolIntoDatabase(ClassNameSymbolType, null, constructClassIdentity(owner), owner, null)
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
        MapperFeature.insertSymbolIntoDatabase(MethodNameSymbolType, null, methodIdentity, name, null)
        MapperFeature.insertSymbolIntoDatabase(ClassNameSymbolType, null, constructClassIdentity(owner), owner, null)
        MapperFeature.insertReferenceIntoDatabase(
            MethodInstructionReferenceType,
            file,
            methodIdentity,
            this.owner,
            null
        )
    }
}