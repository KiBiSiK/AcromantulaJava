package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.java.mapping.ClassMapperContext
import net.cydhra.acromantula.java.mapping.types.ClassNameSymbol
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Visit a class node and extract all symbols and references from it
 *
 * @param file the file of this class node
 * @param context the mapping job context where the mapping is created
 */
class MappingClassVisitor(
    private val file: FileEntity, private val context: ClassMapperContext
) : CustomClassVisitor {

    // the int id of the unique identity of this class
    private lateinit var classIdentity: String

    override suspend fun visitClass(
        version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?
    ) {
        classIdentity = constructClassIdentity(name)
        context.addSymbol(
            classIdentity, ClassNameSymbol(
                identity = classIdentity, sourceFile = file, isInterface = false, // TODO how do we check this
                isAnnotation = false, // TODO how do we check this
                className = name
            )
        )
        if (superName != null) {
            context.addSymbol(
                constructClassIdentity(superName), ClassNameSymbol(
                    identity = superName,
                    sourceFile = null,
                    isInterface = false, // TODO will this be false if this class is an interface too?
                    isAnnotation = false,
                    className = superName
                )
            )
        }

        interfaces?.forEach { itf ->
            context.addSymbol(
                constructClassIdentity(itf), ClassNameSymbol(
                    identity = itf, sourceFile = null, isInterface = true, isAnnotation = false, className = itf
                )
            )
        }
    }

    override suspend fun visitAnnotation(desc: String, values: List<Any>) {

    }

    override suspend fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?) {
//        context.addSymbol(constructFieldIdentity(classIdentity, name, descriptor))
    }

    override suspend fun visitMethod(
        access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?
    ): CustomMethodVisitor? {
//        context.addSymbol(constructMethodIdentity(classIdentity, name, descriptor))

        return IdentityMethodVisitor(file, context)
    }

}