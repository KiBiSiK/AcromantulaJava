package net.cydhra.acromantula.java.mapping.database

import org.jetbrains.exposed.dao.id.IntIdTable

object JavaIdentifierTable : IntIdTable() {
    val identifier = varchar("identifier", Short.MAX_VALUE.toInt())
}