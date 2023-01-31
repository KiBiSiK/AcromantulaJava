package net.cydhra.acromantula.java.mapping

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.cydhra.acromantula.features.mapper.AcromantulaReference
import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.features.mapper.FileMapper
import net.cydhra.acromantula.java.mapping.types.ClassNameSymbol
import net.cydhra.acromantula.java.mapping.types.FieldNameSymbol
import net.cydhra.acromantula.java.mapping.types.MethodNameSymbol
import net.cydhra.acromantula.java.mapping.visitors.MappingClassVisitor
import net.cydhra.acromantula.java.mapping.visitors.accept
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

/**
 * Generate mappings for symbols and references within a class file
 */
class JavaClassMapper : FileMapper<ClassMapperContext> {
    companion object {
        const val ASM_VERSION = Opcodes.ASM9
        private val logger = LogManager.getLogger()
    }

    override fun initializeMapper(file: String): ClassMapperContext {
        // a simple heuristic to initialize the identity cache with a reasonable size
        return when {
            file.endsWith(".class") -> ClassMapperContext(1 shl 7)
            file.endsWith(".jar") || file.endsWith(".zip") || file.endsWith(".war") -> ClassMapperContext(1 shl 12)
            else -> ClassMapperContext(16)
        }
    }

    override suspend fun mapFile(file: FileEntity, content: ByteArray?, state: ClassMapperContext?) {
        if (content == null) {
            // no need to map directories
            return
        }

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
        classNode.accept(MappingClassVisitor(file, state!!))

        // todo references mapping
    }

    override suspend fun getSymbolsInFile(
        file: FileEntity
    ): Collection<AcromantulaSymbol> {
        return withContext(Dispatchers.IO) {
            ClassNameSymbol.getFromFile(file)
                .union(FieldNameSymbol.getFromFile(file))
                .union(MethodNameSymbol.getFromFile(file))
        }
    }

    override suspend fun getReferencesInFile(
        file: FileEntity
    ): Collection<AcromantulaReference> {
        TODO("not implemented")
    }

    override suspend fun getReferencesToSymbol(symbol: AcromantulaSymbol): Collection<AcromantulaReference> {
        TODO("not implemented")
    }

    private fun checkMagicBytes(content: ByteArray): Boolean {
        if (content.size < 4) return false

        return content.contentEquals(byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte()))
    }
}