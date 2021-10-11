package net.cydhra.acromantula.java.view.disassembly

import net.cydhra.acromantula.features.view.document.AcromantulaDocument
import net.cydhra.acromantula.features.view.document.STYLE_KEYWORD
import net.cydhra.acromantula.features.view.document.STYLE_PARENTHESIS
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.w3c.dom.Document

/**
 * Can load either a disassembly or a class node and convert them into each other
 */
class ClassAssembly private constructor(private val assembledClassNode: ClassNode) : Assembly() {

    companion object {
        private const val D_CLASS_VISIBILITY = "visibility"
        private const val D_CLASS_MODIFIERS = "modifiers"
        private const val D_CLASS_CLASS_TYPE = "kind"

        fun fromClassNode(classNode: ClassNode): ClassAssembly {
            return ClassAssembly(classNode)
        }

        fun fromClassView(document: Document): ClassAssembly {
            TODO()
        }

    }

    private fun generateDocument(): AcromantulaDocument {
        return AcromantulaDocument().apply {
            block {
                header {
                    line {
                        checkFlagElse(assembledClassNode.access, Opcodes.ACC_PUBLIC, {
                            f(STYLE_KEYWORD, D_CLASS_VISIBILITY) { +"public" }
                        }, {
                            f(STYLE_KEYWORD, D_CLASS_VISIBILITY) { +"package-private" }
                        })

                        f(STYLE_KEYWORD, D_CLASS_MODIFIERS) { +modifiersToString(assembledClassNode.access) }

                        f(STYLE_KEYWORD, D_CLASS_CLASS_TYPE) {
                            checkFlagElse(assembledClassNode.access, Opcodes.ACC_INTERFACE, {
                                content = "interface"
                            }, {
                                checkFlagElse(assembledClassNode.access, Opcodes.ACC_ANNOTATION, {
                                    content = "annotation"
                                }, {
                                    checkFlagElse(assembledClassNode.access, Opcodes.ACC_MODULE, {
                                        content = "module"
                                    }, {
                                        content = "class"
                                    })
                                })
                            })
                        }

                        f(STYLE_PARENTHESIS) { +"{" }
                    }
                }


                // TODO handle modules
                assembledClassNode.fields.forEach { field ->
                    FieldAssembly.fromFieldNode(field).appendToDocument(this@block)
                }

                footer {
                    line {
                        f(STYLE_PARENTHESIS) { +"}" }
                    }
                }
            }
        }
    }

    private fun modifiersToString(access: Int): String {
        val modifiers = mutableListOf<String>()

        // TODO: present super modifier somehow?
        checkFlag(access, Opcodes.ACC_ABSTRACT) { modifiers += "abstract" }
        checkFlag(access, Opcodes.ACC_SYNTHETIC) { modifiers += "synthetic" }
        checkFlag(access, Opcodes.ACC_FINAL) { modifiers += "final" }

        return modifiers.joinToString(" ")
    }
}