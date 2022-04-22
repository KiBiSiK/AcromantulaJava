package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.types.*
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ParameterNode

/**
 * A method visitor that generates [net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference]s from
 * instructions within the method
 *
 * @param file the database file entity
 * @param owner the method symbol identifier that is used as the owning symbol
 */
class MapperMethodVisitor(private val file: FileEntity, private val owner: String) {

    suspend fun visitParameter(parameterNode: ParameterNode) {

    }

    suspend fun visitTypeInsn(opcode: Int, type: String) {
        val typeIdentity = constructClassIdentity(type)
        MapperFeature.insertSymbolIntoDatabase(ClassNameSymbol, null, typeIdentity, type, null)
        MapperFeature.insertReferenceIntoDatabase(TypeInstructionReference, file, typeIdentity, this.owner, null)
    }

    suspend fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        val fieldIdentity = constructFieldIdentity(constructClassIdentity(owner), name, descriptor)
        MapperFeature.insertSymbolIntoDatabase(FieldNameSymbol, null, fieldIdentity, name, null)
        MapperFeature.insertSymbolIntoDatabase(ClassNameSymbol, null, constructClassIdentity(owner), owner, null)
        MapperFeature.insertReferenceIntoDatabase(FieldInstructionReference, file, fieldIdentity, this.owner, null)
    }

    suspend fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        val methodIdentity = constructMethodIdentity(owner, name, descriptor)
        MapperFeature.insertSymbolIntoDatabase(MethodNameSymbol, null, methodIdentity, name, null)
        MapperFeature.insertSymbolIntoDatabase(ClassNameSymbol, null, constructClassIdentity(owner), owner, null)
        MapperFeature.insertReferenceIntoDatabase(
            MethodInstructionReference,
            file,
            methodIdentity,
            this.owner,
            null
        )
    }

    suspend fun visitReturnType(returnType: Type) {
        val classIdentity = constructClassIdentity(returnType.internalName)
        MapperFeature.insertReferenceIntoDatabase(
            ReturnTypeReference,
            file,
            classIdentity,
            this.owner,
            null
        )
    }

    suspend fun visitParameterType(parameterType: Type) {
        val classIdentity = constructClassIdentity(parameterType.internalName)
        MapperFeature.insertReferenceIntoDatabase(
            ParameterTypeReference,
            file,
            classIdentity,
            this.owner,
            null
        )
    }
}