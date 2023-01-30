package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.objectweb.asm.commons.Remapper

object JavaMethodTable : IntIdTable() {
    val identifier = reference("identifier", JavaIdentifierTable)
    val name = varchar("name", Short.MAX_VALUE.toInt())
    val sourceFile = reference("file", FileTable)
}

class MethodNameSymbol() : AcromantulaSymbol {

    override val canBeRenamed: Boolean
        get() = true

    override val sourceFile: FileEntity
        get() = TODO("not yet implemented")

    override fun getName(): String {
        TODO("not yet implemented")
//        return methodName
    }

    override suspend fun updateName(newName: String) {
//        val (classIdentity, _, methodDescriptor) = transaction {
//            reconstructMethodDefinition(identifier.value)
//        }
//
//        val allReferences = MapperFeature.getReferencesToSymbol(this)
//        for (reference in allReferences) {
//            reference.onUpdateSymbolName(newName)
//        }
//
//        AsmRemappingHelper.remapScheduledFiles(
//            this,
//            MethodNameRemapper(reconstructClassName(classIdentity), methodName, methodDescriptor, newName)
//        )
//
//        transaction {
//            methodName = newName
//            identifier.value = constructMethodIdentity(classIdentity, newName, methodDescriptor)
//        }
        TODO("not yet implemented")
    }

    override fun displayString(): String {
//        return "visibility returnType $methodName descriptor"
        TODO("not yet implemented")
    }

    /**
     * Remaps all references to a method to a new name
     */
    class MethodNameRemapper(
        private val owner: String,
        private val oldName: String,
        private val descriptor: String,
        private val newName: String
    ) : Remapper() {

        override fun mapMethodName(owner: String, name: String, descriptor: String): String {
            if (owner == this.owner && name == this.oldName && descriptor == this.descriptor) {
                return newName
            }

            return super.mapMethodName(owner, name, descriptor)
        }
    }
}