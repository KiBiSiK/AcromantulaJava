package net.cydhra.acromantula.java.mapping

import net.cydhra.acromantula.features.mapper.MappingFactory
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.objectweb.asm.ClassReader

class JavaClassMapper : MappingFactory {
    override val name: String = "java-class-mapper"

    override fun handles(file: FileEntity, content: ByteArray): Boolean {
        TODO("not implemented")
    }

    override suspend fun generateMappings(file: FileEntity, content: ByteArray) {
        val reader = ClassReader(content)
    }
}