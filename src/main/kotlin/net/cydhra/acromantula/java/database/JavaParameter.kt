package net.cydhra.acromantula.java.database

import org.jetbrains.exposed.dao.id.IntIdTable


class JavaParameter

internal object JavaParameterTable : IntIdTable("JavaParameters") {
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
    val method = reference("method", JavaMethodTable)
    val type = reference("type", JavaIdentifierTable)
}