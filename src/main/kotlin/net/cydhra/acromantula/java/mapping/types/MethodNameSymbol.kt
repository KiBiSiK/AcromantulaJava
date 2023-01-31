package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.java.util.Visibility
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.objectweb.asm.commons.Remapper

object JavaMethodTable : IntIdTable() {
    val identifier = reference("identifier", JavaIdentifierTable)
    val access = integer("access")
    val name = varchar("name", Short.MAX_VALUE.toInt())
    val descriptor = varchar("descriptor", Short.MAX_VALUE.toInt())
    val signature = varchar("signature", Short.MAX_VALUE.toInt()).nullable()
    val sourceFile = integer("source_file").nullable()
}

class MethodNameSymbol(
    val identifier: JavaIdentifier,
    val access: Int,
    private var methodName: String,
    val descriptor: String,
    val signature: String?,
    override val sourceFile: FileEntity?
) : JavaSymbol() {

    companion object {
        /**
         * Select all class name symbols from a file. This method must not be used during a mapping job
         *
         * @param fileEntity a file that has already been mapped
         */
        fun getFromFile(fileEntity: FileEntity): List<MethodNameSymbol> {
            return WorkspaceService.databaseTransaction {
                (JavaMethodTable leftJoin JavaIdentifierTable).select { JavaMethodTable.sourceFile eq fileEntity.resource }
                    .map { result ->
                        MethodNameSymbol(
                            identifier = JavaIdentifier(result[JavaIdentifierTable.identifier]).apply {
                                databaseId = result[JavaIdentifierTable.id]
                            },
                            access = result[JavaMethodTable.access],
                            methodName = result[JavaMethodTable.name],
                            descriptor = result[JavaMethodTable.descriptor],
                            signature = result[JavaMethodTable.signature],
                            sourceFile = result[JavaMethodTable.sourceFile]?.let { WorkspaceService.queryPath(it) })
                    }
            }
        }
    }

    override val canBeRenamed: Boolean
        get() = true

    override fun getName(): String {
        return methodName
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

    override fun writeIntoDatabase() {
        WorkspaceService.databaseTransaction {
            JavaMethodTable.insert {
                it[identifier] = this@MethodNameSymbol.identifier.databaseId
                it[access] = this@MethodNameSymbol.access
                it[name] = this@MethodNameSymbol.methodName
                it[descriptor] = this@MethodNameSymbol.descriptor
                it[signature] = this@MethodNameSymbol.signature
                it[sourceFile] = this@MethodNameSymbol.sourceFile?.resource
            }
        }
    }

    override fun displayString(): String {
        return listOfNotNull(
            Visibility.fromAccess(access).token, signature?.let { "<$it>" }, methodName, descriptor
        ).joinToString(" ")
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