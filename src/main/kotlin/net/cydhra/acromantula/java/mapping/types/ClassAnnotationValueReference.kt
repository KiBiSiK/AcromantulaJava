package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity

class ClassAnnotationValueReference(referencedSymbol: AcromantulaSymbol, sourceFile: FileEntity) :
    JavaReference(referencedSymbol, sourceFile) {

    override val referenceType: String
        get() = "java.class.annotation.value"

    override fun displayString(): String {
        TODO("not implemented")
    }
}