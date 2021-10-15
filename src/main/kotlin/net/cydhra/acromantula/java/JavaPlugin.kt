package net.cydhra.acromantula.java

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.features.transformer.TransformerFeature
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.java.mapping.JavaClassMapper
import net.cydhra.acromantula.java.mapping.types.*
import net.cydhra.acromantula.java.transfomers.analysis.cp.ConstantPropagationTransformer
import net.cydhra.acromantula.java.view.disassembly.DisassemblyViewGenerator
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
        MapperFeature.registerSymbolType(MethodNameSymbolType)
        MapperFeature.registerSymbolType(FieldNameSymbolType)

        MapperFeature.registerReferenceType(TypeInstructionReferenceType, ClassNameSymbolType)
        MapperFeature.registerReferenceType(FieldInstructionReferenceType, FieldNameSymbolType)
        MapperFeature.registerReferenceType(MethodInstructionReferenceType, MethodNameSymbolType)

        GenerateViewFeature.registerViewGenerator(DisassemblyViewGenerator)

        TransformerFeature.registerTransformer(ConstantPropagationTransformer())

        logger.info("registered java plugin")
    }

}