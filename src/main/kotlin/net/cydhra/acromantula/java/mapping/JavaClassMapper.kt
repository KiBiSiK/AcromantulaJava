package net.cydhra.acromantula.java.mapping

import net.cydhra.acromantula.features.mapper.MappingFactory
import net.cydhra.acromantula.java.mapping.visitors.MapperClassVisitor
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

/**
 * Generate mappings for symbols and references within a class file
 */
class JavaClassMapper : MappingFactory {
    companion object {
        const val ASM_VERSION = Opcodes.ASM9
        private val logger = LogManager.getLogger()
    }

    override val name: String = "java-class-mapper"

    override fun handles(file: FileEntity, content: ByteArray): Boolean {
        return checkMagicBytes(content)
    }

    override suspend fun generateMappings(file: FileEntity, content: ByteArray) {
        val classNode = ClassNode()
        try {
            ClassReader(content).accept(classNode, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        } catch (e: Exception) {
            logger.error("error while class parsing", e)
        }

        // map the class using the mapper visitor implementation
        classNode.accept(MapperClassVisitor(file))
    }

    private fun checkMagicBytes(content: ByteArray): Boolean {
        if (content.size < 4)
            return false

        return content.copyOfRange(0, 4)
            .contentEquals(byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte()))
    }
}