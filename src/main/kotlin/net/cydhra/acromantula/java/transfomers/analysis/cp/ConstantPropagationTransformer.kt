package net.cydhra.acromantula.java.transfomers.analysis.cp

import net.cydhra.acromantula.features.transformer.FileTransformer
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.apache.logging.log4j.LogManager.getLogger as logger

class ConstantPropagationTransformer : FileTransformer {
    override val name: String = "java.constant-propagation"

    override suspend fun transform(fileEntity: FileEntity) {
        // TODO proper handling

        val reader = ClassReader(WorkspaceService.getFileContent(fileEntity))
        val node = ClassNode()
        reader.accept(node, 0)

        val analyzer = ConstantPropagationAnalysis()

        node.methods.forEach { method ->
            logger().debug("analyzing ${method.name}:")
            analyzer.analyzeMethod(node.name, method)
            logger().debug("... done")
        }

        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        node.accept(writer)
        WorkspaceService.updateFileEntry(fileEntity, writer.toByteArray())
    }
}