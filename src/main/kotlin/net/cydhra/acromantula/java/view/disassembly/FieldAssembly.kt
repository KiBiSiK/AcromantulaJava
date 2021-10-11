package net.cydhra.acromantula.java.view.disassembly

import net.cydhra.acromantula.features.view.document.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode

class FieldAssembly private constructor(private val field: FieldNode) : Assembly() {

    companion object {
        // all field fragment designations

        private const val D_FIELD_SIGNATURE = "signature"
        private const val D_FIELD_VISIBILITY = "visibility"
        private const val D_FIELD_MODIFIER = "modifier"
        private const val D_FIELD_TYPE = "type"
        private const val D_FIELD_NAME = "name"
        private const val D_FIELD_INITIAL_VALUE = "value"

        private const val D_FIELD_ATTRIBUTE_NAME = "attr_name"
        private const val D_FIELD_ATTRIBUTE_CONTENT = "attr_content"

        private const val D_FIELD_INVIS_ANNOTATION = "invis_annotation"
        private const val D_FIELD_VIS_ANNOTATION = "annotation"

        fun fromFieldNode(field: FieldNode): FieldAssembly {
            return FieldAssembly(field)
        }
    }

    fun appendToDocument(block: AcromantulaDocumentBlock) {
        block.block {
            if (field.signature != null) {
                line {
                    f {
                        style = STYLE_ANNOTATION
                        designation = D_FIELD_SIGNATURE
                        content = field.signature
                    }
                }
            }

            if (field.attrs != null) {
                field.attrs.forEach { attr ->
                    line {
                        f {
                            style = STYLE_ANNOTATION
                            designation = D_FIELD_ATTRIBUTE_NAME
                            content = attr.type
                        }
                        f {
                            +"= ???"
                        }
                    }
                }
            }

            // dump invisible annotations
            if (field.invisibleAnnotations != null) {
                field.invisibleAnnotations.forEach { annotationNode ->
                    line {
                        designation = D_FIELD_INVIS_ANNOTATION
                        AnnotationAssembly.fromAnnotationNode(annotationNode).appendToLine(this)
                    }
                }
            }

            // dump visible annotations
            if (field.visibleAnnotations != null) {
                field.visibleAnnotations.forEach { annotationNode ->
                    line {
                        designation = D_FIELD_VIS_ANNOTATION
                        AnnotationAssembly.fromAnnotationNode(annotationNode).appendToLine(this)
                    }
                }
            }

            // TODO type annotations

            line {
                f(STYLE_KEYWORD, D_FIELD_VISIBILITY) { +visibilityToString(field.access) }
                f(STYLE_KEYWORD, D_FIELD_MODIFIER) { +modifiersToString(field.access) }
                f(STYLE_TYPE, D_FIELD_TYPE) { +field.desc }
                f(STYLE_IDENTIFIER, D_FIELD_NAME) { +field.name }

                if (field.value != null) {
                    f { +"=" }
                    f {
                        style = if (field.value is String)
                            STYLE_LITERAL_STRING
                        else
                            STYLE_LITERAL_NUMBER

                        designation = D_FIELD_INITIAL_VALUE
                        field.value
                    }
                }

                f(STYLE_PUNCTUATION) { +";" }
            }
        }
    }

    private fun visibilityToString(access: Int): String {
        checkFlag(access, Opcodes.ACC_PUBLIC) { return "public" }
        checkFlag(access, Opcodes.ACC_PRIVATE) { return "private" }
        checkFlag(access, Opcodes.ACC_PROTECTED) { return "protected" }
        return "package-private"
    }

    private fun modifiersToString(access: Int): String {
        val modifiers = mutableListOf<String>()

        checkFlag(access, Opcodes.ACC_STATIC) { modifiers += "static" }
        checkFlag(access, Opcodes.ACC_FINAL) { modifiers += "final" }
        checkFlag(access, Opcodes.ACC_VOLATILE) { modifiers += "volatile" }
        checkFlag(access, Opcodes.ACC_TRANSIENT) { modifiers += "transient" }
        checkFlag(access, Opcodes.ACC_SYNTHETIC) { modifiers += "synthetic" }
        checkFlag(access, Opcodes.ACC_ENUM) { modifiers += "variant" }

        return modifiers.joinToString(" ")
    }
}
