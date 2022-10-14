package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.java.util.reconstructClassName
import net.cydhra.acromantula.java.util.reconstructMethodDefinition
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.objectweb.asm.commons.Remapper

object JavaMethodTable : IntIdTable() {
    val identifier = reference("identifier", JavaIdentifierTable)
    val name = varchar("name", Short.MAX_VALUE.toInt())
    val sourceFile = reference("file", FileTable)
}

class MethodNameSymbol(id: EntityID<Int>) : IntEntity(id), AcromantulaSymbol {
    companion object : IntEntityClass<MethodNameSymbol>(JavaMethodTable)

    override val canBeRenamed: Boolean
        get() = true

    /**
     * Unique java symbol identifier entity. Access with database transaction
     */
    var identifier by JavaIdentifier referencedOn JavaMethodTable.identifier

    override var sourceFile by FileEntity referencedOn JavaMethodTable.sourceFile

    /**
     * Method name. Update only via [updateName]
     */
    var methodName by JavaMethodTable.name

    override fun getName(): String {
        return methodName
    }

    override suspend fun updateName(newName: String) {
        val (classIdentity, _, methodDescriptor) = transaction {
            reconstructMethodDefinition(identifier.value)
        }

        val allReferences = MapperFeature.getReferencesToSymbol(this)
        for (reference in allReferences) {
            reference.onUpdateSymbolName(newName)
        }

        AsmRemappingHelper.remapScheduledFiles(
            this,
            MethodNameRemapper(reconstructClassName(classIdentity), methodName, methodDescriptor, newName)
        )

        transaction {
            methodName = newName
            identifier.value = constructMethodIdentity(classIdentity, newName, methodDescriptor)
        }
    }

    override fun displayString(): String {
        return "visibility returnType $methodName descriptor"
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