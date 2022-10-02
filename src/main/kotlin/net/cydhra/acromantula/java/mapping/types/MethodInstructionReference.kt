package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity

class MethodInstructionReference(
    referencedSymbol: AcromantulaSymbol,
    sourceFile: FileEntity
) : JavaReference(referencedSymbol, sourceFile) {
    override val referenceType: String
        get() = "java.insn.method"

    override fun displayString(): String {
        TODO("not implemented")
    }
}