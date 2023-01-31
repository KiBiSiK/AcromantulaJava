package net.cydhra.acromantula.java.util

import org.objectweb.asm.Opcodes

enum class Visibility(val token: String?) {
    PUBLIC("public"), PROTECTED("protected"), PACKAGE_PRIVATE(null), PRIVATE("private");

    companion object {
        /**
         * Retrieve [Visibility] from Bytecode access flags.
         */
        fun fromAccess(access: Int): Visibility = when {
            access and Opcodes.ACC_PUBLIC > 0 -> PUBLIC
            access and Opcodes.ACC_PRIVATE > 0 -> PRIVATE
            access and Opcodes.ACC_PROTECTED > 0 -> PROTECTED
            else -> PACKAGE_PRIVATE
        }
    }
}

enum class ClassKind(val token: String) {
    CLASS("class"), ENUM("enum"), INTERFACE("interface"), ANNOTATION("@annotation"), MODULE("module");

    companion object {

        /**
         * Get a class kind from its access flags
         */
        fun fromAccess(access: Int): ClassKind = when {
            access and Opcodes.ACC_INTERFACE > 0 -> INTERFACE
            access and Opcodes.ACC_ANNOTATION > 0 -> ANNOTATION
            access and Opcodes.ACC_ENUM > 0 -> ENUM
            access and Opcodes.ACC_MODULE > 0 -> MODULE
            else -> CLASS
        }
    }
}