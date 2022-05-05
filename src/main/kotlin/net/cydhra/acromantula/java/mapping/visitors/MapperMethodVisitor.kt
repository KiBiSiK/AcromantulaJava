package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.types.*
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.java.util.isPrimitive
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
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

    suspend fun visitInvokeDynamicInsn(opcode: Int, name: String, desc: String, bsm: Handle, bsmArgs: Array<Any>) {
        // todo: are the method name and descriptor from the dynamically computed callsite
        //  relevant for method dispatch? Or in other words: can we rename the function of the
        //  functional interface used in the invokedynamic call without renaming the parameters
        //  in the callsite and still get the dynamic call to work? (if so, we not necessarily need to map it)
//        val bootstrapMethod = constructMethodIdentity(constructClassIdentity(bsm.owner), bsm.name, bsm.desc)
//        MapperFeature.insertSymbolIntoDatabase(MethodNameSymbol, null, bootstrapMethod, bsm.name, null)

        bsmArgs.filterIsInstance<Handle>().forEach { handle ->
            when (handle.tag) {
                Opcodes.H_GETFIELD,
                Opcodes.H_GETSTATIC,
                Opcodes.H_PUTFIELD,
                Opcodes.H_PUTSTATIC -> {
                    val fieldHandle =
                        constructFieldIdentity(constructClassIdentity(handle.owner), handle.name, handle.desc)

                    MapperFeature.insertSymbolIntoDatabase(FieldNameSymbol, null, fieldHandle, bsm.name, null)
                    MapperFeature.insertReferenceIntoDatabase(
                        InvokeDynamicFieldReference,
                        file,
                        fieldHandle,
                        this.owner,
                        null
                    )
                }
                Opcodes.H_INVOKEVIRTUAL,
                Opcodes.H_INVOKESTATIC,
                Opcodes.H_INVOKESPECIAL,
                Opcodes.H_NEWINVOKESPECIAL,
                Opcodes.H_INVOKEINTERFACE -> {
                    val methodHandle =
                        constructMethodIdentity(constructClassIdentity(handle.owner), handle.name, handle.desc)

                    MapperFeature.insertSymbolIntoDatabase(MethodNameSymbol, null, methodHandle, bsm.name, null)
                    MapperFeature.insertReferenceIntoDatabase(
                        InvokeDynamicMethodReference,
                        file,
                        methodHandle,
                        this.owner,
                        null
                    )
                }
            }


        }
    }

    suspend fun visitReturnType(returnType: Type) {
        if (isPrimitive(returnType))
            return

        val classIdentity = constructClassIdentity(returnType.internalName)
        MapperFeature.insertSymbolIntoDatabase(
            ClassNameSymbol,
            null,
            classIdentity,
            returnType.internalName,
            null
        )
        MapperFeature.insertReferenceIntoDatabase(
            ReturnTypeReference,
            file,
            classIdentity,
            this.owner,
            null
        )
    }

    suspend fun visitParameterType(parameterType: Type) {
        if (isPrimitive(parameterType))
            return

        val classIdentity = constructClassIdentity(parameterType.internalName)
        MapperFeature.insertSymbolIntoDatabase(
            ClassNameSymbol,
            null,
            classIdentity,
            parameterType.internalName,
            null
        )
        MapperFeature.insertReferenceIntoDatabase(
            ParameterTypeReference,
            file,
            classIdentity,
            this.owner,
            null
        )
    }
}