package net.cydhra.acromantula.java.mapping.visitors

/**
 * A custom visitor interface for ASM class nodes, because the provided one is not detailed enough.
 */
interface CustomClassVisitor {

    suspend fun visitClass(
        version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?
    )

    suspend fun visitAnnotation(desc: String, values: List<Any>)

    suspend fun visitField(
        access: Int, name: String, descriptor: String, signature: String?, value: Any?
    )

    suspend fun visitMethod(
        access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?
    ): CustomMethodVisitor?
}