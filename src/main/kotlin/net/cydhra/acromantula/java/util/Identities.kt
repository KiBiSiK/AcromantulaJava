package net.cydhra.acromantula.java.util

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

/**
 * @return a string that uniquely identifies the class. If two classes exist, that cannot coexist within the same
 * classloader, they may share the same identity
 */
fun ClassNode.getIdentity(): String {
    return constructClassIdentity(this.name)
}

/**
 * @param owner the [ClassNode] that is the owner of the field.
 *
 * @return a string that uniquely identifies the field. If two fields exist, that cannot coexist within the same
 * classloader, they may share the same identity
 */
fun FieldNode.getIdentity(owner: ClassNode): String {
    return constructFieldIdentity(owner.getIdentity(), this.name, this.desc)
}

/**
 * @param owner the [ClassNode] that is the owner of the method.
 *
 * @return a string that uniquely identifies the method. If two methods exist, that cannot coexist within the same
 * classloader, they may share the same identity
 */
fun MethodNode.getIdentity(owner: ClassNode): String {
    return constructMethodIdentity(owner.getIdentity(), this.name, this.desc)
}

/**
 * Construct a string that uniquely identifies a java class
 *
 * @param fullName the fully qualified JVM internal class name
 *
 * @return a class-identifying string
 */
fun constructClassIdentity(fullName: String): String {
    return fullName
}

/**
 * Construct a string that uniquely identifies a java field member
 *
 * @param ownerIdentity the unique class identity of the field's class
 *
 * @return a field-identifying string
 */
fun constructFieldIdentity(ownerIdentity: String, fieldName: String, fieldDescriptor: String): String {
    return "$ownerIdentity::$fieldName:$fieldDescriptor"
}

/**
 * Construct a string that uniquely identifies a java method member
 *
 * @param ownerIdentity the unique class identity of the method's class
 *
 * @return a method-identifying string
 */
fun constructMethodIdentity(ownerIdentity: String, methodName: String, methodDescriptor: String): String {
    return "$ownerIdentity::$methodName$methodDescriptor"
}