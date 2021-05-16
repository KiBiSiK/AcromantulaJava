package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.types.*
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
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
    suspend fun visitTypeInsn(opcode: Int, type: String) {
        val typeIdentity = constructClassIdentity(type)
        val symbol = MapperFeature.getSymbolFromDatabase(ClassNameSymbolType, typeIdentity, typeIdentity)
        MapperFeature.insertReferenceIntoDatabase(TypeInstructionReferenceType, file, symbol, owner, null)
    }

    suspend fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        val fieldIdentity = constructFieldIdentity(constructClassIdentity(owner), name, descriptor)
        val symbol = MapperFeature.getSymbolFromDatabase(FieldNameSymbolType, fieldIdentity, name)
        MapperFeature.insertReferenceIntoDatabase(FieldInstructionReferenceType, file, symbol, this.owner, null)
    }

    suspend fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        val methodIdentity = constructMethodIdentity(owner, name, descriptor)
        val symbol = MapperFeature.getSymbolFromDatabase(MethodNameSymbolType, methodIdentity, name)
        MapperFeature.insertReferenceIntoDatabase(MethodInstructionReferenceType, file, symbol, this.owner, null)
    }
}