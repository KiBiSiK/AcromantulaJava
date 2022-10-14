package net.cydhra.acromantula.java

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.features.transformer.TransformerFeature
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.java.mapping.JavaClassMapper
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import net.cydhra.acromantula.java.mapping.types.JavaClassTable
import net.cydhra.acromantula.java.transfomers.analysis.cp.ConstantPropagationTransformer
import net.cydhra.acromantula.java.transfomers.analysis.elimination.DeadVariableEliminationTransformer
import net.cydhra.acromantula.java.view.disassembly.DisassemblyViewGenerator
import net.cydhra.acromantula.plugins.AcromantulaPlugin
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.SchemaUtils

class JavaPlugin : AcromantulaPlugin {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override val author: String = "Cydhra"

    override val name: String = "Java Tool Suite"

    override fun initialize() {
        MapperFeature.registerMapper(JavaClassMapper())

        GenerateViewFeature.registerViewGenerator(DisassemblyViewGenerator)

        TransformerFeature.registerTransformer(ConstantPropagationTransformer())
        TransformerFeature.registerTransformer(DeadVariableEliminationTransformer())

        WorkspaceService.registerAtDatabase {
            SchemaUtils.createMissingTablesAndColumns(
                JavaIdentifierTable,
                JavaClassTable
            )
        }

        logger.info("registered java plugin")
    }

}