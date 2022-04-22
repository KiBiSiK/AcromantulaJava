package net.cydhra.acromantula.java.mapping.types

import net.cydhra.acromantula.features.mapper.AcromantulaSymbolType
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol

object ClassNameSymbol : AcromantulaSymbolType("java.class.name", true) {
    override fun onUpdateName(symbol: ContentMappingSymbol, newName: String) {
        TODO("not implemented")
    }

}