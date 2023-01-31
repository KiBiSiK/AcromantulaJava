package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.insert

object SuperClassTable : IntIdTable() {
    val referencedSymbol = reference("referenced", JavaIdentifierTable, onDelete = ReferenceOption.RESTRICT)
    val sourceFile = integer("source_file")
    val extendingClass = reference("extending", JavaClassTable, onDelete = ReferenceOption.CASCADE)
}

class ClassSuperReference(
    val referencedIdentifier: JavaIdentifier,
    val superClassSymbol: ClassNameSymbol?,
    sourceFile: FileEntity,
    val extendingClass: ClassNameSymbol
) : JavaReference(superClassSymbol, sourceFile) {

    override val referenceType: String
        get() = "java.class.super"

    override fun displayString(): String {
        return "${extendingClass.getName()} extends ${referencedIdentifier.identifier}"
    }

    override fun writeReferenceToDatabase() {
        WorkspaceService.databaseTransaction {
            SuperClassTable.insert {
                it[referencedSymbol] = this@ClassSuperReference.referencedIdentifier.databaseId
                it[sourceFile] = this@ClassSuperReference.sourceFile.resource
                it[extendingClass] = this@ClassSuperReference.extendingClass.databaseId
            }
        }
    }
}