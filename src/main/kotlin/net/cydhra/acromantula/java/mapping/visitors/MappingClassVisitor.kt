package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.java.mapping.ClassMapperContext
import net.cydhra.acromantula.java.mapping.types.ClassNameSymbol
import net.cydhra.acromantula.java.mapping.types.ClassSuperReference
import net.cydhra.acromantula.java.mapping.types.FieldNameSymbol
import net.cydhra.acromantula.java.mapping.types.MethodNameSymbol
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
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

    private lateinit var classSymbol: ClassNameSymbol

    override suspend fun visitClass(
        version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?
    ) {
        classIdentity = constructClassIdentity(name)
        classSymbol = ClassNameSymbol(
            identifier = context.retrieveIdentifier(classIdentity),
            sourceFile = file,
            access = access,
            signature = signature,
            className = name,
        )

        context.addSymbol(classSymbol)

        if (superName != null) {
            val superIdentifier = context.retrieveIdentifier(constructClassIdentity(superName))
            context.addReference(
                ClassSuperReference(
                    referencedIdentifier = superIdentifier,
                    superClassSymbol = null,
                    sourceFile = file,
                    extendingClass = classSymbol
                )
            )
        }
//        interfaces?.forEach { itf ->
//            context.addSymbol(
//                constructClassIdentity(itf), ClassNameSymbol(
//                    identity = itf, sourceFile = null, isInterface = true, isAnnotation = false, className = itf
//                )
//            )
//        }
    }

    override suspend fun visitAnnotation(desc: String, values: List<Any>?) {

    }

    override suspend fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?) {
        context.addSymbol(
            FieldNameSymbol(
                identifier = context.retrieveIdentifier(constructFieldIdentity(classIdentity, name, descriptor)),
                access = access,
                fieldName = name,
                descriptor = descriptor,
                signature = signature,
                sourceFile = file
            )
        )
    }

    override suspend fun visitMethod(
        access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?
    ): CustomMethodVisitor {
        context.addSymbol(
            MethodNameSymbol(
                identifier = context.retrieveIdentifier(constructMethodIdentity(classIdentity, name, descriptor)),
                access = access,
                methodName = name,
                descriptor = descriptor,
                signature = signature,
                sourceFile = file
            )
        )

        return IdentityMethodVisitor(file, context)
    }

}