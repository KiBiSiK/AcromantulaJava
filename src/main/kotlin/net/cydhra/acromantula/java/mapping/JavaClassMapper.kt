package net.cydhra.acromantula.java.mapping

import net.cydhra.acromantula.features.mapper.MappingFactory
import net.cydhra.acromantula.java.mapping.visitors.MapperClassVisitor
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import java.io.PushbackInputStream

/**
 * Generate mappings for symbols and references within a class file
 */
class JavaClassMapper : MappingFactory {
    companion object {
        const val ASM_VERSION = Opcodes.ASM9
        private val logger = LogManager.getLogger()
    }

    override val name: String = "java-class-mapper"

    override fun handles(file: FileEntity, content: PushbackInputStream): Boolean {
        return checkMagicBytes(content)
    }

    override suspend fun generateMappings(file: FileEntity, content: PushbackInputStream) {
        val classNode = ClassNode()
        try {
            ClassReader(content).accept(classNode, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        } catch (e: Exception) {
            logger.error("error while class parsing", e)
        }

        // map the class using the mapper visitor implementation
        with(MapperClassVisitor(file)) {
            visit(
                classNode.version,
                classNode.access,
                classNode.name,
                classNode.signature,
                classNode.superName,
                classNode.interfaces?.toTypedArray()
            )

            classNode.fields.forEach { fieldNode ->
                visitField(
                    fieldNode.access,
                    fieldNode.name,
                    fieldNode.desc,
                    fieldNode.signature,
                    fieldNode.value
                )
            }

            classNode.methods.forEach { methodNode ->
                val visitor = visitMethod(
                    methodNode.access,
                    methodNode.name,
                    methodNode.desc,
                    methodNode.signature,
                    methodNode.exceptions?.toTypedArray()
                )

                with(visitor) {
                    visitReturnType(Type.getReturnType(methodNode.desc))
                    Type.getArgumentTypes(methodNode.desc).forEach { argType ->
                        visitParameterType(argType)
                    }

                    methodNode.instructions.forEach { insn ->
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
                        }
                    }
                }
            }
        }
    }

    private fun checkMagicBytes(content: PushbackInputStream): Boolean {
        val magic = ByteArray(4)
        val readSize = content.read(magic)

        try {
            if (readSize < 4)
                return false

            return magic.contentEquals(byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte()))
        } finally {
            content.unread(magic)
        }
    }
}