package net.cydhra.acromantula.java.util

/**
 * Calculate the return-type of a method descriptor
 */
fun returnType(methodDescriptor: String): String {
    return methodDescriptor.substring(methodDescriptor.lastIndexOf(')') + 1)
}