package net.cydhra.acromantula.java.mapping.visitors

import org.objectweb.asm.Handle
import org.objectweb.asm.Type

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