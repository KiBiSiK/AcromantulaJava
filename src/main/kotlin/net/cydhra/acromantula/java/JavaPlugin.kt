package net.cydhra.acromantula.java

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.features.transformer.TransformerFeature
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.java.mapping.JavaClassMapper
import net.cydhra.acromantula.java.mapping.types.*
import net.cydhra.acromantula.java.transfomers.analysis.cp.ConstantPropagationTransformer
import net.cydhra.acromantula.java.transfomers.analysis.elimination.DeadVariableEliminationTransformer
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

        MapperFeature.registerSymbolType(ClassNameSymbol)
        MapperFeature.registerSymbolType(MethodNameSymbol)
        MapperFeature.registerSymbolType(FieldNameSymbol)

        MapperFeature.registerReferenceType(ClassInstructionReference, ClassNameSymbol)
        MapperFeature.registerReferenceType(FieldInstructionReference, FieldNameSymbol)
        MapperFeature.registerReferenceType(MethodInstructionReference, MethodNameSymbol)
        MapperFeature.registerReferenceType(InvokeDynamicFieldReference, FieldNameSymbol)
        MapperFeature.registerReferenceType(InvokeDynamicMethodReference, MethodNameSymbol)
        MapperFeature.registerReferenceType(ReturnTypeReference, ClassNameSymbol)
        MapperFeature.registerReferenceType(ParameterTypeReference, ClassNameSymbol)
        MapperFeature.registerReferenceType(ClassConstantReference, ClassNameSymbol)
        MapperFeature.registerReferenceType(ClassSuperReference, ClassNameSymbol)
        MapperFeature.registerReferenceType(ClassInterfaceReference, ClassNameSymbol)

        GenerateViewFeature.registerViewGenerator(DisassemblyViewGenerator)

        TransformerFeature.registerTransformer(ConstantPropagationTransformer())
        TransformerFeature.registerTransformer(DeadVariableEliminationTransformer())

        logger.info("registered java plugin")
    }

}