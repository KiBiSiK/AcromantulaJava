package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * First pass visitor for MethodNodes that will just extract all necessary identities from the file. This way when we
 * build references from instructions later, we can be sure that all identities are already present in the database
 * and no conflicts will arise.
 *
 * @param file the class file of this node
 */
class IdentityMethodVisitor(
    private val file: FileEntity,
    private val ownerIdentity: String
) : CustomMethodVisitor {

    override suspend fun visitReturnType(returnType: Type) {
        IdentityCache.insertIdentity(constructClassIdentity(returnType.internalName))
    }

    override suspend fun visitParameterType(parameterType: Type) {
        IdentityCache.insertIdentity(constructClassIdentity(parameterType.internalName))
    }

    override suspend fun visitTypeInsn(opcode: Int, type: String) {
        IdentityCache.insertIdentity(constructClassIdentity(type))
    }

    override suspend fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        IdentityCache.insertIdentity(constructClassIdentity(owner))
        IdentityCache.insertIdentity(constructFieldIdentity(constructClassIdentity(owner), name, descriptor))
    }

    override suspend fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        IdentityCache.insertIdentity(constructClassIdentity(owner))
        IdentityCache.insertIdentity(constructMethodIdentity(constructClassIdentity(owner), name, descriptor))
    }

    override suspend fun visitInvokeDynamicInsn(
        opcode: Int,
        name: String,
        desc: String,
        bsm: Handle,
        bsmArgs: Array<Any>
    ) {
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
                    IdentityCache.insertIdentity(
                        constructFieldIdentity(
                            constructClassIdentity(handle.owner),
                            handle.name,
                            handle.desc
                        )
                    )
                }

                Opcodes.H_INVOKEVIRTUAL,
                Opcodes.H_INVOKESTATIC,
                Opcodes.H_INVOKESPECIAL,
                Opcodes.H_NEWINVOKESPECIAL,
                Opcodes.H_INVOKEINTERFACE -> {
                    IdentityCache.insertIdentity(
                        constructMethodIdentity(constructClassIdentity(handle.owner), handle.name, handle.desc)
                    )
                }
            }


        }
    }

    override suspend fun visitLdcInsn(opcode: Int, constant: Any) {
        if (constant is Type) {
            IdentityCache.insertIdentity(constructClassIdentity(constant.internalName))
        }
    }
}