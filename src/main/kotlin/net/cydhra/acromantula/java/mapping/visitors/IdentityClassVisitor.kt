package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * First pass visitor for ClassNodes that will just extract all necessary identities from the file. This way when we
 * build references from instructions later, we can be sure that all identities are already present in the database
 * and no conflicts will arise.
 *
 * @param file the class file of this node
 */
class IdentityClassVisitor(
    private val file: FileEntity,
) : CustomClassVisitor {

    // the int id of the unique identity of this class
    lateinit var classIdentity: String

    override suspend fun visitClass(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        classIdentity = constructClassIdentity(name)
        IdentityCache.insertIdentity(classIdentity)
        if (superName != null)
            IdentityCache.insertIdentity(constructClassIdentity(superName))

        interfaces?.forEach { itf -> IdentityCache.insertIdentity(constructClassIdentity(itf)) }
    }

    override suspend fun visitAnnotation(desc: String, values: List<Any>) {

    }

    override suspend fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?) {
        IdentityCache.insertIdentity(constructFieldIdentity(classIdentity, name, descriptor))
    }

    override suspend fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): CustomMethodVisitor? {
        IdentityCache.insertIdentity(constructMethodIdentity(classIdentity, name, descriptor))

        return IdentityMethodVisitor(file, classIdentity)
    }

}