package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * References to a class type within the list of implemented interfaces of a type
 */
class ClassInterfaceReference(
    referencedSymbol: AcromantulaSymbol,
    sourceFile: FileEntity
) : JavaReference(referencedSymbol, sourceFile) {
    override val referenceType: String
        get() = "java.class.itf"

    override fun displayString(): String {
        TODO("not implemented")
    }

    override fun writeReferenceToDatabase() {
        TODO("Not yet implemented")
    }
}