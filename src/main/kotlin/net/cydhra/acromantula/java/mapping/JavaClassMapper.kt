package net.cydhra.acromantula.java.mapping

import net.cydhra.acromantula.features.mapper.AcromantulaReference
import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.features.mapper.FileMapper
import net.cydhra.acromantula.java.mapping.visitors.MapperClassVisitor
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

/**
 * Generate mappings for symbols and references within a class file
 */
class JavaClassMapper : FileMapper {
    companion object {
        const val ASM_VERSION = Opcodes.ASM9
        private val logger = LogManager.getLogger()
    }

    override suspend fun mapFile(file: FileEntity, content: ByteArray) {
        if (!checkMagicBytes(content.slice(0 until 4).toTypedArray().toByteArray())) {
            return
        }

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

            classNode.visibleAnnotations?.forEach { ann -> visitAnnotation(ann.desc, ann.values) }
            classNode.invisibleAnnotations?.forEach { ann -> visitAnnotation(ann.desc, ann.values) }
            classNode.visibleTypeAnnotations?.forEach { ann -> visitAnnotation(ann.desc, ann.values) }
            classNode.invisibleTypeAnnotations?.forEach { ann -> visitAnnotation(ann.desc, ann.values) }

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
            }
        }
    }

    override suspend fun getSymbolsInFile(
        file: FileEntity,
        predicate: ((AcromantulaSymbol) -> Boolean)?
    ): Collection<AcromantulaSymbol> {
        TODO("not implemented")
    }

    override suspend fun getReferencesInFile(
        file: FileEntity,
        predicate: ((AcromantulaReference) -> Boolean)?
    ): Collection<AcromantulaReference> {
        TODO("not implemented")
    }

    override suspend fun getReferencesToSymbol(symbol: AcromantulaSymbol): Collection<AcromantulaReference> {
        TODO("not implemented")
    }

    private fun checkMagicBytes(content: ByteArray): Boolean {
        if (content.size < 4)
            return false

        return content.contentEquals(byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte()))
    }
}