package net.cydhra.acromantula.java.mapping.visitors

import jdk.internal.org.objectweb.asm.Type
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.types.*
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * @param file the database file handle
 */
class MapperClassVisitor(private val file: FileEntity) {
    /**
     * Unique class identity
     */
    private lateinit var identity: String

    suspend fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        this.identity = constructClassIdentity(name)
        MapperFeature.insertSymbolIntoDatabase(
            ClassNameSymbol,
            this.file,
            this.identity,
            name,
            null
        )

        // class file name referencing the class symbol
        MapperFeature.insertReferenceIntoDatabase(
            ClassFileReference,
            this.file,
            this.identity,
            name,
            null
        )

        if (superName != null) {
            val superIdentity = constructClassIdentity(superName)
            MapperFeature.insertSymbolIntoDatabase(
                ClassNameSymbol,
                null,
                superIdentity,
                superName,
                null
            )
            MapperFeature.insertReferenceIntoDatabase(
                ClassSuperReference,
                file,
                superIdentity,
                this.identity,
                null
            )
        }

        interfaces?.forEach { itf ->
            val itfIdentity = constructClassIdentity(itf)
            MapperFeature.insertSymbolIntoDatabase(
                ClassNameSymbol,
                null,
                itfIdentity,
                itf,
                null
            )

            MapperFeature.insertReferenceIntoDatabase(
                ClassSuperReference,
                file,
                itfIdentity,
                this.identity,
                null
            )
        }
    }

    suspend fun visitAnnotation(desc: String, values: List<Any>) {
        val annotationType = Type.getType(desc)
        val classIdentity = constructClassIdentity(annotationType.internalName)
        MapperFeature.insertSymbolIntoDatabase(ClassNameSymbol, null, classIdentity, annotationType.internalName, null)

        MapperFeature.insertReferenceIntoDatabase(
            ClassAnnotationTypeReference,
            file,
            classIdentity,
            identity,
            null
        )

        for (i in (0..values.size).step(2)) {
            if (values[1] is Type) {
                val valueType = Type.getType((values[1] as Type).descriptor)
                val valueClassIdentity = constructClassIdentity(annotationType.internalName)
                MapperFeature.insertSymbolIntoDatabase(
                    ClassNameSymbol,
                    null,
                    valueClassIdentity,
                    valueType.internalName,
                    null
                )

                MapperFeature.insertReferenceIntoDatabase(
                    ClassAnnotationValueReference,
                    file,
                    valueClassIdentity,
                    identity,
                    null
                )
            }
        }
    }

    suspend fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): MapperMethodVisitor? {
        MapperFeature.insertSymbolIntoDatabase(
            FieldNameSymbol,
            this.file,
            constructFieldIdentity(this.identity, name, descriptor),
            name,
            null
        )

        return null
    }

    suspend fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MapperMethodVisitor {
        val methodIdentity = constructMethodIdentity(this.identity, name, descriptor)
        MapperFeature.insertSymbolIntoDatabase(
            MethodNameSymbol,
            this.file,
            methodIdentity,
            name,
            null
        )

        return MapperMethodVisitor(file, methodIdentity)
    }
}