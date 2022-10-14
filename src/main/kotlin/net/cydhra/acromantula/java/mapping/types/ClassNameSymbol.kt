package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.database.JavaIdentifier
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.java.mapping.remapping.AsmRemappingHelper
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.transactions.transaction
import org.objectweb.asm.commons.Remapper

/**
 * A table for all java classes, interfaces and similar class-like constructs. It references an identifier that
 * uniquely describes this class
 */
object JavaClassTable : IntIdTable() {
    val identifier = reference("identifier", JavaIdentifierTable.identifier, onDelete = ReferenceOption.RESTRICT)
    val name = varchar("name", Short.MAX_VALUE.toInt())
    val file = reference("file", FileTable, onDelete = ReferenceOption.CASCADE)
    val isInterface = bool("isInterface")
    val isAnnotation = bool("isAnnotation")
}

class ClassNameSymbol(id: EntityID<Int>) : IntEntity(id), AcromantulaSymbol {

    override val canBeRenamed: Boolean
        get() = true

    /**
     * Source file of this class symbol. Access with database transaction
     */
    override var sourceFile by FileEntity referencedOn JavaClassTable.file

    /**
     * Unique java symbol identifier entity. Access with database transaction
     */
    var identifier by JavaIdentifier referencedOn JavaClassTable.identifier

    /**
     * Whether this class file is an interface definition. Do not update.
     */
    var isInterface by JavaClassTable.isInterface

    /**
     * Whether this class file is an annotation definition. Do not update.
     */
    var isAnnotation by JavaClassTable.isAnnotation

    /**
     * Qualified class name. Update using [updateName]
     */
    private var className by JavaClassTable.name

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

        transaction {
            // update identifier
            identifier.value = constructClassIdentity(newName)

            // update class name
            this@ClassNameSymbol.className = newName
        }
    }

    override fun displayString(): String {
        return "visibility ${
            when {
                isInterface -> "interface"
                isAnnotation -> "@annotation class"
                else -> "class"
            }
        } $className"
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