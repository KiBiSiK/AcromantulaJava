package net.cydhra.acromantula.java.util

import org.objectweb.asm.Type

/**
 * Calculate the return-type of a method descriptor
 */
fun returnType(methodDescriptor: String): String {
    return methodDescriptor.substring(methodDescriptor.lastIndexOf(')') + 1)
}

/**
 * Whether the given type is a primitive type or primitive array type
 */
fun isPrimitive(type: Type): Boolean {
    if (type.sort == Type.ARRAY) {
        return isPrimitive(type.elementType)
    }

    return type.sort < Type.ARRAY
}