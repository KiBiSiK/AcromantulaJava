package net.cydhra.acromantula.java.view.disassembly

import net.cydhra.acromantula.features.view.document.AcromantulaDocumentBlock
import net.cydhra.acromantula.features.view.document.STYLE_ANNOTATION
import net.cydhra.acromantula.features.view.document.STYLE_PARENTHESIS
import net.cydhra.acromantula.features.view.document.STYLE_PUNCTUATION
import org.objectweb.asm.tree.AnnotationNode

class AnnotationAssembly private constructor(private val annotationNode: AnnotationNode) : Assembly() {

    companion object {

        private const val D_ANNOTATION_DESCRIPTOR = "desc"

        /**
         * Generate assembly from an [AnnotationAssembly]
         */
        fun fromAnnotationNode(node: AnnotationNode): AnnotationAssembly {
            return AnnotationAssembly(node)
        }
    }

    fun appendToLine(line: AcromantulaDocumentBlock.LineBuilder) {
        line.f(STYLE_ANNOTATION, D_ANNOTATION_DESCRIPTOR) {
            content = annotationNode.desc
        }
        line.f(STYLE_PARENTHESIS) { +"(" }

        annotationNode.values.chunked(2) { it.first() to it.last() }.forEach { (key, value) ->
            // TODO type descriptor

            line.f {
                +key.toString()
            }
            line.f {
                +"="
            }

            // TODO const style formatting
            line.f {
                +value.toString()
            }

            // TODO no space before this
            line.f(STYLE_PUNCTUATION) { +"," }
        }

        line.f(STYLE_PARENTHESIS) { +")" }
    }
}