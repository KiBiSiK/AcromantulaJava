package net.cydhra.acromantula.java.mapping.visitors

import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.java.mapping.JavaClassMapper
import net.cydhra.acromantula.java.mapping.types.ClassNameSymbolType
import net.cydhra.acromantula.java.mapping.types.FieldNameSymbolType
import net.cydhra.acromantula.java.mapping.types.MethodNameSymbolType
import net.cydhra.acromantula.java.util.constructClassIdentity
import net.cydhra.acromantula.java.util.constructFieldIdentity
import net.cydhra.acromantula.java.util.constructMethodIdentity
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor

/**
 * @param file the database file handle
 */
class MapperClassVisitor(private val file: FileEntity) : ClassVisitor(JavaClassMapper.ASM_VERSION) {
    /**
     * Unique class identity
     */
    private lateinit var identity: String

    /**
     * Class name database symbol
     */
    private lateinit var symbol: ContentMappingSymbol

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        this.identity = constructClassIdentity(name)
        this.symbol = MapperFeature.insertSymbolIntoDatabase(
            ClassNameSymbolType,
            this.file,
            this.identity,
            name,
            null
        )
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        MapperFeature.insertSymbolIntoDatabase(
            FieldNameSymbolType,
            this.file,
            constructFieldIdentity(this.identity, name, descriptor),
            name,
            null
        )

        return null
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val symbol = MapperFeature.insertSymbolIntoDatabase(
            MethodNameSymbolType,
            this.file,
            constructMethodIdentity(this.identity, name, descriptor),
            name,
            null
        )

        return MapperMethodVisitor(file, symbol)
    }
}