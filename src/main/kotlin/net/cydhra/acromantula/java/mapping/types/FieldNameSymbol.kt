package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.reconstructClassName
import net.cydhra.acromantula.java.util.reconstructFieldDefinition
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.objectweb.asm.commons.Remapper

object JavaFieldTable : IntIdTable() {
    val identifier = reference("identifier", JavaIdentifierTable)
    val name = varchar("name", Short.MAX_VALUE.toInt())
    val sourceFile = reference("file", FileTable)
}

class FieldNameSymbol(id: EntityID<Int>) : IntEntity(id), AcromantulaSymbol {
    companion object : IntEntityClass<FieldNameSymbol>(JavaFieldTable)

    override val canBeRenamed: Boolean
        get() = true

    /**
     * Unique java symbol identifier entity. Access with database transaction
     */
    var identifier by JavaIdentifier referencedOn JavaFieldTable.identifier

    /**
     * Field name. Do not update directly, call [updateName]
     */
    var fieldName by JavaFieldTable.name

    override val sourceFile
        get() = TODO("not yet implemented")

    override fun getName(): String {
        return fieldName
    }

    override suspend fun updateName(newName: String) {
        val (classIdentity, _, fieldDescriptor) = reconstructFieldDefinition(identifier.value)

        val allReferences = MapperFeature.getReferencesToSymbol(this)
        for (reference in allReferences) {
            reference.onUpdateSymbolName(newName)
        }

        AsmRemappingHelper.remapScheduledFiles(
            this,
            FieldNameRemapper(reconstructClassName(classIdentity), fieldName, fieldDescriptor, newName)
        )

        transaction {
            fieldName = newName

            identifier.value = constructFieldIdentity(classIdentity, newName, fieldDescriptor)
        }
    }

    override fun displayString(): String {
        return "visibility type $fieldName"
    }

    /**
     * Remaps all references to a field to a new name
     */
    class FieldNameRemapper(
        private val owner: String,
        private val oldName: String,
        private val descriptor: String,
        private val newName: String
    ) : Remapper() {

        override fun mapFieldName(owner: String, name: String, descriptor: String): String {
            if (owner == this.owner && name == this.oldName && descriptor == this.descriptor) {
                return newName
            }

            return super.mapFieldName(owner, name, descriptor)
        }
    }
}