package net.cydhra.acromantula.java

import net.cydhra.acromantula.plugins.AcromantulaPlugin
import org.apache.logging.log4j.LogManager

class JavaPlugin : AcromantulaPlugin {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override val author: String = "Cydhra"

    override val name: String = "Java Tool Suite"

    override fun initialize() {
        logger.info("registered java plugin")
    }

}