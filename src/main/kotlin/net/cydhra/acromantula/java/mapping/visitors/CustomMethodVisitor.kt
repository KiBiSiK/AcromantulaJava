package net.cydhra.acromantula.java.mapping.visitors

import org.objectweb.asm.Handle
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

interface CustomMethodVisitor {

    suspend fun visitReturnType(returnType: Type)

    suspend fun visitParameterType(parameterType: Type)

    suspend fun visitTypeInsn(opcode: Int, type: String)

    suspend fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String)

    suspend fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    )

    suspend fun visitInvokeDynamicInsn(opcode: Int, name: String, desc: String, bsm: Handle, bsmArgs: Array<Any>)

    suspend fun visitLdcInsn(opcode: Int, constant: Any)
}

/**
 * Custom extension to method nodes that dispatches a custom method visitor onto all relevant members of the node
 */
suspend fun MethodNode.accept(customMethodVisitor: CustomMethodVisitor) = with(customMethodVisitor) {
    visitReturnType(Type.getReturnType(this@accept.desc))
    Type.getArgumentTypes(this@accept.desc).forEach { argType ->
        visitParameterType(argType)
    }

    this@accept.instructions.forEach { insn ->
        when (insn) {
            is TypeInsnNode -> visitTypeInsn(insn.opcode, insn.desc)
            is FieldInsnNode -> visitFieldInsn(insn.opcode, insn.owner, insn.name, insn.desc)
            is MethodInsnNode -> visitMethodInsn(
                insn.opcode,
                insn.owner,
                insn.name,
                insn.desc,
                insn.itf
            )

            is InvokeDynamicInsnNode -> visitInvokeDynamicInsn(
                insn.opcode,
                insn.name,
                insn.desc,
                insn.bsm,
                insn.bsmArgs
            )

            is LdcInsnNode -> visitLdcInsn(insn.opcode, insn.cst)
        }
    }
}