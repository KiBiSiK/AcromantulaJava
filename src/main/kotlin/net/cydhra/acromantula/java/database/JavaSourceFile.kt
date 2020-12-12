package net.cydhra.acromantula.java.database

import org.jetbrains.exposed.dao.IntIdTable

class JavaSourceFile

internal object JavaSourceFileTable : IntIdTable("JavaSourceFiles") {
    val path = varchar("path", MAX_IDENTIFIER_LENGTH)
}