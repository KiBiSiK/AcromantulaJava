package net.cydhra.acromantula.java.view.disassembly

import com.strobel.assembler.ir.OpCode
import net.cydhra.acromantula.features.view.document.*
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

class MethodAssembly private constructor(private val method: MethodNode) : Assembly() {

    companion object {
        // all field fragment designations

        private const val D_METHOD_SIGNATURE = "signature"
        private const val D_METHOD_VISIBILITY = "visibility"
        private const val D_METHOD_MODIFIER = "modifier"
        private const val D_METHOD_DESCRIPTOR = "desc"
        private const val D_METHOD_NAME = "name"
        private const val D_METHOD_EXCEPTION = "exception"

        private const val D_METHOD_ATTRIBUTE_NAME = "attr_name"
        private const val D_METHOD_ATTRIBUTE_CONTENT = "attr_content"

        private const val D_METHOD_INVIS_ANNOTATION = "invis_annotation"
        private const val D_METHOD_VIS_ANNOTATION = "annotation"

        private const val D_METHOD_INSTRUCTION = "insn"
        private const val D_METHOD_INSTRUCTION_OPERAND = "op"

        fun fromMethodNode(field: MethodNode): MethodAssembly {
            return MethodAssembly(field)
        }
    }

    fun appendToDocument(block: AcromantulaDocumentBlock) {
        block.block {
            header {
                if (method.signature != null) {
                    line {
                        f {
                            style = STYLE_ANNOTATION
                            designation = D_METHOD_SIGNATURE
                            content = method.signature
                        }
                    }
                }

                if (method.attrs != null) {
                    method.attrs.forEach { attr ->
                        line {
                            f {
                                style = STYLE_ANNOTATION
                                designation = D_METHOD_ATTRIBUTE_NAME
                                content = attr.type
                            }
                            f {
                                designation = D_METHOD_ATTRIBUTE_CONTENT
                                +"= ???"
                            }
                        }
                    }
                }

                // dump invisible annotations
                if (method.invisibleAnnotations != null) {
                    method.invisibleAnnotations.forEach { annotationNode ->
                        line {
                            designation = D_METHOD_INVIS_ANNOTATION
                            AnnotationAssembly.fromAnnotationNode(annotationNode).appendToLine(this)
                        }
                    }
                }

                // dump visible annotations
                if (method.visibleAnnotations != null) {
                    method.visibleAnnotations.forEach { annotationNode ->
                        line {
                            designation = D_METHOD_VIS_ANNOTATION
                            AnnotationAssembly.fromAnnotationNode(annotationNode).appendToLine(this)
                        }
                    }
                }

                line {
                    f(STYLE_KEYWORD, D_METHOD_VISIBILITY) { +visibilityToString(method.access) }
                    f(STYLE_KEYWORD, D_METHOD_MODIFIER) { +modifiersToString(method.access) }

                    f(STYLE_IDENTIFIER, D_METHOD_NAME) { +method.name }
                    f(STYLE_TYPE, D_METHOD_DESCRIPTOR) { +method.desc }

                    if (method.exceptions.isNotEmpty()) {
                        f { +"throws" }
                        for (type in method.exceptions) {
                            f(STYLE_TYPE, D_METHOD_EXCEPTION) {
                                +type
                            }
                        }
                    }

                    f(STYLE_PARENTHESIS) { +"{" }
                }
            }

            // TODO method.annotationDefault

            // TODO: designations, annotations, scopes
            if (method.localVariables != null && method.localVariables.isNotEmpty()) {
                block {
                    header { line { +"Local Variables:" } }
                    method.localVariables.forEach {
                        line {
                            f { +"${it.index}:" }
                            if (it.signature != null)
                                f(STYLE_ANNOTATION) { +it.signature }
                            f(STYLE_DESCRIPTOR) { +it.desc }
                            f(STYLE_IDENTIFIER) { +it.name }
                        }
                    }
                }
            }

            for (ins in method.instructions) {
                dumpInstruction(this, ins)
            }

            footer { line { f(STYLE_PARENTHESIS) { +"}" } } }
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

        checkFlag(access, Opcodes.ACC_ABSTRACT) { modifiers += "abstract" }
        checkFlag(access, Opcodes.ACC_STATIC) { modifiers += "static" }
        checkFlag(access, Opcodes.ACC_FINAL) { modifiers += "final" }
        checkFlag(access, Opcodes.ACC_SYNCHRONIZED) { modifiers += "synchronized" }
        checkFlag(access, Opcodes.ACC_STRICT) { modifiers += "strictfp" }
        checkFlag(access, Opcodes.ACC_SYNTHETIC) { modifiers += "synthetic" }
        checkFlag(access, Opcodes.ACC_BRIDGE) { modifiers += "bridge" }
        checkFlag(access, Opcodes.ACC_VARARGS) { modifiers += "varargs" }
        checkFlag(access, Opcodes.ACC_NATIVE) { modifiers += "native" }

        return modifiers.joinToString(" ")
    }

    private fun dumpInstruction(block: AcromantulaDocumentBlock, instruction: AbstractInsnNode) {
        block.line {
            designation = D_METHOD_INSTRUCTION

            if (instruction is LabelNode || instruction is FrameNode || instruction is LineNumberNode) {
                // TODO handle labels and frames
                f { +"todo: label/frames" }
                return@line
            }

            f(STYLE_OPERATION) {
                +OpCode.get(instruction.opcode).name
            }

            when (instruction) {
                is IntInsnNode -> f(
                    STYLE_LITERAL_NUMBER,
                    D_METHOD_INSTRUCTION_OPERAND
                ) { +instruction.operand.toString() }
                is VarInsnNode -> f(
                    STYLE_LITERAL_NUMBER,
                    D_METHOD_INSTRUCTION_OPERAND
                ) { +instruction.`var`.toString() }
                is TypeInsnNode -> f(STYLE_DESCRIPTOR, D_METHOD_INSTRUCTION_OPERAND) {
                    // TODO type annotations
                    +instruction.desc
                }
                is FieldInsnNode -> f {
                    // TODO type annotations
                    designation = D_METHOD_INSTRUCTION_OPERAND
                    f(STYLE_TYPE) { +instruction.owner }
                    f(STYLE_PUNCTUATION) { +"::" }
                    f(STYLE_IDENTIFIER) { +instruction.name }
                    f(STYLE_DESCRIPTOR) { +instruction.desc }
                }
                is MethodInsnNode -> f {
                    // TODO type annotations
                    designation = D_METHOD_INSTRUCTION_OPERAND
                    if (instruction.itf)
                        f { +"itf" }
                    f(STYLE_TYPE) { +instruction.owner }
                    f(STYLE_PUNCTUATION) { +"::" }
                    f(STYLE_IDENTIFIER) { +instruction.name }
                    f(STYLE_DESCRIPTOR) { +instruction.desc }
                }
                is InvokeDynamicInsnNode -> f {
                    // TODO type annotations
                    dumpHandle(this, instruction.bsm)

                    f { +"(" }
                    for (bsmArg in instruction.bsmArgs) {
                        when (bsmArg) {
                            is String -> f(STYLE_LITERAL_STRING) { +bsmArg.toString() }
                            is Type -> f(STYLE_TYPE) { +bsmArg.descriptor }
                            is Handle -> dumpHandle(this, bsmArg)
                            else -> f(STYLE_LITERAL_NUMBER) { +bsmArg.toString() }
                        }
                    }
                    f { +")" }

                    f(STYLE_PUNCTUATION) { +"::" }
                    f(STYLE_IDENTIFIER) { +instruction.name }
                    f(STYLE_DESCRIPTOR) { +instruction.desc }
                }
                is JumpInsnNode -> f {
                    +instruction.label.toString()
                }
                is LabelNode -> f(STYLE_LABEL) {
                    +instruction.toString()
                }
                is LdcInsnNode -> when (instruction.cst) {
                    is String -> f(STYLE_LITERAL_STRING) { +instruction.cst.toString() }
                    is Type -> f(STYLE_TYPE) { +(instruction.cst as Type).descriptor }
                    else -> f(STYLE_LITERAL_NUMBER) { +instruction.cst.toString() }
                }

                is IincInsnNode -> f {
                    f(STYLE_LITERAL_NUMBER) {
                        +instruction.`var`.toString()
                    }
                    f(STYLE_LITERAL_NUMBER) {
                        +instruction.incr.toString()
                    }
                }
                is TableSwitchInsnNode -> f {
                    f(STYLE_LITERAL_NUMBER) { instruction.min }
                    f(STYLE_LITERAL_NUMBER) { instruction.max }

                    for (label in instruction.labels) {
                        line {
                            style = STYLE_LABEL
                            +label.toString()
                        }
                    }
                    line {
                        style = STYLE_LABEL
                        +"default "
                        +instruction.dflt.toString()
                    }
                }
                is LookupSwitchInsnNode -> f {
                    f(STYLE_PARENTHESIS) { +"[" }
                    for (key in instruction.keys) {
                        f(STYLE_LITERAL_NUMBER) { +key.toString() }
                    }
                    f(STYLE_PARENTHESIS) { +"]" }

                    for (label in instruction.labels) {
                        line {
                            style = STYLE_LABEL
                            +label.toString()
                        }
                    }
                }
                is MultiANewArrayInsnNode -> f {
                    f(STYLE_LITERAL_NUMBER) {
                        +instruction.dims.toString()
                    }
                    f(STYLE_DESCRIPTOR) {
                        +instruction.desc
                    }
                }
            }
        }
    }

    private fun dumpHandle(fragment: AcromantulaDocumentBlock.FragmentBuilder, handle: Handle) {
        fragment.f {
            f {
                when (handle.tag) {
                    Opcodes.H_GETFIELD -> +"getfield"
                    Opcodes.H_GETSTATIC -> +"getstatic"
                    Opcodes.H_PUTFIELD -> +"putfield"
                    Opcodes.H_PUTSTATIC -> +"putstatic"
                    Opcodes.H_INVOKEVIRTUAL -> +"invokevirtual"
                    Opcodes.H_INVOKESTATIC -> +"invokestatic"
                    Opcodes.H_INVOKESPECIAL -> +"invokespecial"
                    Opcodes.H_NEWINVOKESPECIAL -> +"new"
                    Opcodes.H_INVOKEINTERFACE -> +"invokeinterface"
                    else -> {
                        style = STYLE_ERROR
                        +"error: unsupported method (${handle.tag})"
                    }
                }
            }

            f(STYLE_PARENTHESIS) { +"[" }
            f(STYLE_TYPE) { +handle.owner }
            f(STYLE_PUNCTUATION) { +"::" }
            f(STYLE_TYPE) { +handle.name }
            f(STYLE_DESCRIPTOR) { +handle.desc }

            f(STYLE_PARENTHESIS) { +"]" }
        }
    }
}
