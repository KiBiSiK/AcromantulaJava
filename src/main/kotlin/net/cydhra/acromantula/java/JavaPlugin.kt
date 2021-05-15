package net.cydhra.acromantula.java

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.ClassNameSymbolType
import net.cydhra.acromantula.java.mapping.JavaClassMapper
import net.cydhra.acromantula.plugins.AcromantulaPlugin
import org.apache.logging.log4j.LogManager

class JavaPlugin : AcromantulaPlugin {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override val author: String = "Cydhra"

    override val name: String = "Java Tool Suite"

    override fun initialize() {
        MapperFeature.registerMappingFactory(JavaClassMapper())
        MapperFeature.registerSymbolType(ClassNameSymbolType)
        logger.info("registered java plugin")
    }

}