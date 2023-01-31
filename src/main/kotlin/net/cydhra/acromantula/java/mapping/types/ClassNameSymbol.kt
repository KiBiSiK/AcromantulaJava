package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.java.util.ClassKind
import net.cydhra.acromantula.java.util.Visibility
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.objectweb.asm.commons.Remapper

/**
 * A table for all java classes, interfaces and similar class-like constructs. It references an identifier that
 * uniquely describes this class
 */
object JavaClassTable : IntIdTable() {
    val identifier = reference("identifier", JavaIdentifierTable, onDelete = ReferenceOption.RESTRICT)
    val access = integer("access")
    val name = varchar("name", Short.MAX_VALUE.toInt())
    val signature = varchar("signature", Short.MAX_VALUE.toInt()).nullable()
    val sourceFile = integer("file").nullable()
}

class ClassNameSymbol(
    val identifier: JavaIdentifier,
    val access: Int,
    private var className: String,
    val signature: String?,
    override val sourceFile: FileEntity?,
) : JavaSymbol() {

    companion object {
        /**
         * Select all class name symbols from a file. This method must not be used during a mapping job
         *
         * @param fileEntity a file that has already been mapped
         */
        fun getFromFile(fileEntity: FileEntity): List<ClassNameSymbol> {
            return WorkspaceService.databaseTransaction {
                (JavaClassTable leftJoin JavaIdentifierTable).select { JavaClassTable.sourceFile eq fileEntity.resource }
                    .map { result ->
                        ClassNameSymbol(
                            identifier = JavaIdentifier(result[JavaIdentifierTable.identifier]).apply {
                                databaseId = result[JavaIdentifierTable.id]
                            },
                            access = result[JavaClassTable.access],
                            className = result[JavaClassTable.name],
                            signature = result[JavaClassTable.signature],
                            sourceFile = result[JavaClassTable.sourceFile]?.let { WorkspaceService.queryPath(it) })
                    }
            }
        }
    }

    override val canBeRenamed: Boolean
        get() = true

    /**
     * Get qualified class name
     */
    override fun getName(): String {
        return this.className
    }


    override suspend fun updateName(newName: String) {
        val oldPath = this.className.substring(0, this.className.lastIndexOf("/") + 1)
        if (!newName.startsWith(oldPath) || newName.removePrefix(oldPath).contains('/')) {
            throw IllegalArgumentException("moving files is not yet supported")
        }

        val allReferences = MapperFeature.getReferencesToSymbol(this)
        for (reference in allReferences) {
            reference.onUpdateSymbolName(newName)
        }

        AsmRemappingHelper.remapScheduledFiles(
            this, ClassNameRemapper(this.className, newName)
        )

        TODO("not yet implemented")
        // update identifier
//            identifier.value = constructClassIdentity(newName)

        // update class name
        this@ClassNameSymbol.className = newName
    }

    override fun displayString(): String {
        return listOfNotNull(Visibility.fromAccess(access).token,
            ClassKind.fromAccess(access).token,
            className,
            signature?.let { "<$it>" }).joinToString(" ")
    }

    override fun writeIntoDatabase() {
        WorkspaceService.databaseTransaction {
            JavaClassTable.insert {
                it[identifier] = this@ClassNameSymbol.identifier.databaseId
                it[access] = this@ClassNameSymbol.access
                it[name] = this@ClassNameSymbol.className
                it[signature] = this@ClassNameSymbol.signature
                it[sourceFile] = this@ClassNameSymbol.sourceFile?.resource
            }
        }
    }

    class ClassNameRemapper(
        private val oldName: String, private val newName: String
    ) : Remapper() {
        override fun mapType(internalName: String): String {
            if (internalName == oldName) {
                return newName
            }
            return super.mapType(internalName)
        }

        override fun map(internalName: String): String {
            if (internalName == oldName) {
                return newName
            }
            return super.map(internalName)
        }
    }
}
