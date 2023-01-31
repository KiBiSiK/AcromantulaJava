package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.java.util.Visibility
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.objectweb.asm.commons.Remapper

object JavaFieldTable : IntIdTable() {
    val identifier = reference("identifier", JavaIdentifierTable)
    val access = integer("access")
    val name = varchar("name", Short.MAX_VALUE.toInt())
    val descriptor = varchar("descriptor", Short.MAX_VALUE.toInt())
    val signature = varchar("signature", Short.MAX_VALUE.toInt()).nullable()
    val sourceFile = integer("file")
}

class FieldNameSymbol(
    val access: Int,
    val identifier: JavaIdentifier,
    private var fieldName: String,
    val descriptor: String,
    val signature: String?,
    override var sourceFile: FileEntity
) : JavaSymbol() {

    companion object {
        /**
         * Select all field name symbols from a file. This method must not be used during a mapping job
         *
         * @param fileEntity a file that has already been mapped
         */
        fun getFromFile(fileEntity: FileEntity): List<FieldNameSymbol> {
            return WorkspaceService.databaseTransaction {
                (JavaFieldTable leftJoin JavaIdentifierTable).select { JavaFieldTable.sourceFile eq fileEntity.resource }
                    .map { result ->
                        FieldNameSymbol(
                            identifier = JavaIdentifier(result[JavaIdentifierTable.identifier]).apply {
                                databaseId = result[JavaIdentifierTable.id]
                            },
                            access = result[JavaFieldTable.access],
                            fieldName = result[JavaFieldTable.name],
                            descriptor = result[JavaFieldTable.descriptor],
                            signature = result[JavaFieldTable.signature],
                            sourceFile = WorkspaceService.queryPath(result[JavaFieldTable.sourceFile])
                        )
                    }
            }
        }
    }

    override val canBeRenamed: Boolean
        get() = true

    override fun getName(): String {
        return fieldName
    }


    override suspend fun updateName(newName: String) {
//        val (classIdentity, _, fieldDescriptor) = reconstructFieldDefinition(identifier.value)
//
//        val allReferences = MapperFeature.getReferencesToSymbol(this)
//        for (reference in allReferences) {
//            reference.onUpdateSymbolName(newName)
//        }
//
//        AsmRemappingHelper.remapScheduledFiles(
//            this,
//            FieldNameRemapper(reconstructClassName(classIdentity), fieldName, fieldDescriptor, newName)
//        )
//
//        transaction {
//            fieldName = newName
//
//            identifier.value = constructFieldIdentity(classIdentity, newName, fieldDescriptor)
//        }
        TODO("not yet implemented")
    }

    override fun displayString(): String {
        return listOfNotNull(Visibility.fromAccess(access),
            fieldName,
            descriptor,
            signature?.let { "<$it>" }).joinToString(" ")
    }

    override fun writeIntoDatabase(): EntityID<Int> {
        return WorkspaceService.databaseTransaction {
            JavaFieldTable.insertAndGetId {
                it[identifier] = this@FieldNameSymbol.identifier.databaseId
                it[access] = this@FieldNameSymbol.access
                it[name] = this@FieldNameSymbol.fieldName
                it[descriptor] = this@FieldNameSymbol.descriptor
                it[signature] = this@FieldNameSymbol.signature
                it[sourceFile] = this@FieldNameSymbol.sourceFile.resource
            }
        }
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