package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.types.ClassNameSymbol
import net.cydhra.acromantula.java.mapping.types.FieldNameSymbol
import net.cydhra.acromantula.java.mapping.types.MethodNameSymbol
import net.cydhra.acromantula.java.mapping.types.SuperClassReference
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
                SuperClassReference,
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
                SuperClassReference,
                file,
                itfIdentity,
                this.identity,
                null
            )
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