package net.cydhra.acromantula.java.view.disassembly

import net.cydhra.acromantula.features.view.ViewGeneratorStrategy
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileType
import net.cydhra.acromantula.workspace.filesystem.textFileType
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.ByteArrayOutputStream
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object DisassemblyViewGenerator : ViewGeneratorStrategy {
    override val viewType: String = "java.code.disassembly"
    override val fileType: FileType = textFileType

    override val supportsReconstruction: Boolean = true

    override fun reconstructFromView(fileEntity: FileEntity, buffer: ByteArray) {
        super.reconstructFromView(fileEntity, buffer)
    }

    override fun handles(fileEntity: FileEntity): Boolean {
        return fileEntity.name.endsWith(".class")
    }

    override fun generateView(fileEntity: FileEntity): FileViewEntity {
        val reader = ClassReader(WorkspaceService.getFileContent(fileEntity))
        val node = ClassNode()
        reader.accept(node, 0)

        val document = ClassAssembly.fromClassNode(node).generateDocument().finishDocument()
        document.normalizeDocument()

        val transformer = TransformerFactory.newInstance().newTransformer()
        val output = ByteArrayOutputStream()
        transformer.transform(DOMSource(document), StreamResult(output))

        return WorkspaceService.addFileRepresentation(fileEntity, this.viewType, output.toByteArray())
    }
}