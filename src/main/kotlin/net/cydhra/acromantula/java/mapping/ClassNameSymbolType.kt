package net.cydhra.acromantula.java.mapping

import net.cydhra.acromantula.features.mapper.AcromantulaSymbolType
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol

object ClassNameSymbolType : AcromantulaSymbolType("java.class.name", true) {
    override fun onUpdateName(symbol: ContentMappingSymbol, newName: String) {
        TODO("not implemented")
    }

}