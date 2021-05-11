package net.cydhra.acromantula.java.database

import org.jetbrains.exposed.dao.id.IntIdTable


class JavaModule

internal object JavaModuleTable : IntIdTable("JavaModule") {
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
}