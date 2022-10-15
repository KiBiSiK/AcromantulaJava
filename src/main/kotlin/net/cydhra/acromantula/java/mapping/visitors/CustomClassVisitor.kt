package net.cydhra.acromantula.java.mapping.visitors

import org.objectweb.asm.tree.ClassNode

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

suspend fun ClassNode.accept(customClassVisitor: CustomClassVisitor) = with(customClassVisitor) {
    visitClass(
        this@accept.version,
        this@accept.access,
        this@accept.name,
        this@accept.signature,
        this@accept.superName,
        this@accept.interfaces?.toTypedArray()
    )

    this@accept.visibleAnnotations?.forEach { ann -> visitAnnotation(ann.desc, ann.values) }
    this@accept.invisibleAnnotations?.forEach { ann -> visitAnnotation(ann.desc, ann.values) }
    this@accept.visibleTypeAnnotations?.forEach { ann -> visitAnnotation(ann.desc, ann.values) }
    this@accept.invisibleTypeAnnotations?.forEach { ann -> visitAnnotation(ann.desc, ann.values) }

    this@accept.fields.forEach { fieldNode ->
        visitField(
            fieldNode.access,
            fieldNode.name,
            fieldNode.desc,
            fieldNode.signature,
            fieldNode.value
        )
    }

    this@accept.methods.forEach { methodNode ->
        val visitor = visitMethod(
            methodNode.access,
            methodNode.name,
            methodNode.desc,
            methodNode.signature,
            methodNode.exceptions?.toTypedArray()
        )

        if (visitor != null) {
            methodNode.accept(visitor)
        }
    }
}